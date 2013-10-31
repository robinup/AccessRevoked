#!/usr/bin/env python

'''
dataprep.py

Utility functions for accessing Vertica data

Author: Yunzhi Zhou <yunzhi.zhou@tapjoy.com>
'''

import datetime
import logging
import math

import pyodbc

def connect_vertica():
    return pyodbc.connect('DSN=VerticaDSN; UID=dbadmin; PWD=TJ4ever!', ansi = 'False')

def query_vertica(sql, conn = None):
    if not conn:
        conn = connect_vertica()
    cursor = conn.cursor()
    
    logging.info('Vertica query: %s' % sql)
    
    cursor.execute(sql)
    
    ret = []
    for r in cursor.fetchall():
        ret.append(r)
        
    return ret



def load_offers():
    '''
    Output: [id, offer_name, bid, payment, devices]
    '''
    
    sql = '''
select
    id, offer_name, bid, payment, device_types
from
    optimization.offers
where
    item_type != 'DeeplinkOffer'
    and
    user_enabled = 1
    and
    tapjoy_enabled = 1
    '''
    ret = query_vertica(sql)
    
    return [[r[0], r[1], r[2], r[3], eval(r[4])] for r in ret]


def calculate_ecpm(days = 30):
    '''
    Output: 
        {
            segment: [id, offer_name, total_conversion, total_imp, eCPM, total_rev, adjusted_eCPM]
        }
    '''
    #slogger = logging.getLogger(name)
    startdate = datetime.datetime.now() - datetime.timedelta(days = 30)
    
    segments = [('gen_iOS', 'offerwall', 'iOS'), ('gen_Android', 'offerwall', 'Android'), 
                ('tjm_iOS', 'tj_games', 'iOS'), ('tjm_Android', 'tj_games', 'Android')]
    ret = {}
    for name, source, os in segments:
        sql = '''
select 
    imp.offer_id, offer.offer_name, 
    IFNULL(conversion.total_conversions, 0) as total_conversion,
    IFNULL(imp.total_imp, 0) as total_imp,
    IFNULL(conversion.total_conversions * offer.rev * 1000 / imp.total_imp, 0.0) as eCPM,
    IFNULL(conversion.total_conversions * offer.rev, 0.0) as total_rev
from
(
    select 
        offer_id, sum(impressions) as total_imp 
    from 
        optimization.offerwall_views_agg 
    where 
        day > '%(startdate)s' and source = '%(source)s' and os = '%(os)s' 
    group by 
        offer_id
) as imp
left join 
(
    select 
        offer_id, count(1) as total_conversions 
    from 
        optimization.offerwall_actions 
    where 
        converted_at > '%(startdate)s' and source = '%(source)s' and os = '%(os)s'
    group by 
        offer_id
) as conversion
on 
    imp.offer_id = conversion.offer_id
join 
(
    select 
        id, offer_name, payment, (payment / 100.00) as rev
    from 
        optimization.offers 
    where
        item_type != 'DeeplinkOffer'
)
as offer
on 
    imp.offer_id = offer.id
;
'''  % {'startdate': startdate, 'source': source, 'os': os}
        ds = [(r[0], r[1], r[2], r[3], r[4], r[5], float(r[4]) * math.pow(math.log10(r[3]) / 3.0, 2)) for r in query_vertica(sql)]
        
        ret[name] = ds
    return ret
    

if __name__ == '__main__':
    import unittest
    
    import sys
    reload(sys)
    
    sys.setdefaultencoding('utf-8')
    
    class TestCase(unittest.TestCase):
        def setUp(self):
            pass
        
        def tearDown(self):
            pass
        
        def test_load_offers(self):
            self.assertNotEqual(0, load_offers(), 'No offer loaded.')
            
        def test_calculate_cpm(self):
            self.assertTrue(calculate_ecpm())
    
    unittest.main()