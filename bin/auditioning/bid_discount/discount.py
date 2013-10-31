#!/usr/bin/env python

import dataprep
import offer_q

def discount_bid(default_q = 0.5):
    '''
    Output: {
                'segment': {'offer_id': (payment, bid_star, bid, q)}
            }
    '''
    offers_data = dataprep.load_offers()
    ecpms_data = dataprep.calculate_ecpm(30)
    
    gen_Android = {}
    gen_iOS = {}
    tjm_Android = {}
    tjm_iOS = {}
    
    ret = {}
        
    for seg in ecpms_data:
        ret[seg] = {}
        ret_seg = ret[seg]
        
        
    
        ecpms = [r[4] for r in ecpms_data[seg]]
        adj_ecpms = [r[6] for r in ecpms_data[seg]]
        ecpm_ids = [r[0] for r in ecpms_data[seg]]
    
        qs = offer_q.q_percentile(ecpms)
        #print qs
        #print ecpm_ids
        qs = dict(zip(ecpm_ids, qs))
        #print qs
    
        for i, offer in enumerate(offers_data):
            id, offer_name, bid, payment, devices = offer
            bid = float(bid)
            payment = float(payment)
            dis = max(bid - payment, 0)
        
            q = qs.get(id, default_q)
        
            if dis:
                bid_star = payment + (1 - q) * dis
                #print seg, id, payment, bid_star, bid, q
                #print 'bingo'
            else:
                bid_star = payment
            ret_seg[id] = (payment, bid_star, bid, q)
        
    return ret


        

if __name__ == '__main__':
    import unittest
    
    import numpy as np
    
    class TestFixture(unittest.TestCase):
        def setUp(self):
            pass
        
        def tearDonw(self):
            pass
        
        def test_discount_bid(self):
            ret = discount_bid()
            flags = []
            for seg in ret:
                seg_val = ret[seg]
                any = False
                for r in seg_val:
                    #print seg, r, seg_val[r]
                    payment, bid_star, bid, q = seg_val[r]
                    if payment != bid and bid_star != bid and q != 0.5:
                        any = True
                flags.append(any)
            #print flags
            self.assertTrue(np.all(flags)) 
        
    unittest.main()