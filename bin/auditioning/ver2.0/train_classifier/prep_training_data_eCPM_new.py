#!/usr/bin/env python

import os
import sys
import datetime,time
from time import gmtime,strftime
import shutil
import decimal
from datetime import timedelta
import pyodbc
import math
cnxn = pyodbc.connect("DSN=VerticaDSN;UID=dbadmin;PWD=TJ4ever!",ansi="False")
cursor = cnxn.cursor()

work_dir = sys.argv[1]
lbw = int(sys.argv[2])
predict_class = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']

#print 'cur_work_dir',cur_work_hr
gen_ios_sql = 'select a.id, a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
gen_android_sql = 'select a.id,a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';                                                                                                                                                                   
tjm_ios_sql = 'select a.id,a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
tjm_android_sql = 'select a.id,a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';


test_data_sql = [gen_ios_sql,gen_android_sql,tjm_ios_sql,tjm_android_sql]
train_data_param1 = ['offerwall','offerwall','tj_games','tj_games']
train_data_param2 = ['iOS','Android','iOS','Android']

idx =0

for ea_class in predict_class:
	#today_hr = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S.0')
	min_timeline1 = datetime.datetime.now() + timedelta(hours=-24*lbw)
	fw_train_ecpm = open(work_dir+ea_class+'_ecpm_new','w')#fw_train_ecpm = open('/ebs/audition/adaboost/'+ea_class+'_ecpm_new','w')
	window_hr =  min_timeline1.strftime('%Y-%m-%d %H:%M:%S.0') #'2012-07-10 12:00:00.0'

	query_ecpm = 'select imp.offer_id,offer.offer_name,IFNULL(conversion.total_conversions,0) as total_conversion,IFNULL(imp.total_imp,0) as total_imp, \
IFNULL(conversion.total_conversions*offer.rev*1000/imp.total_imp,0) as eCPM, IFNULL(conversion.total_conversions*offer.rev,0) as total_rev  from \
(select offer_id,sum(impressions) as total_imp from optimization.offerwall_views_agg where day > \
 \''+ window_hr+'\' and source=\''+ train_data_param1[idx]+'\' and os=\''+ train_data_param2[idx]+'\' group by 1) as imp \
left join (select offer_id,count(*) as total_conversions from optimization.offerwall_actions where converted_at > \
\'' + window_hr+'\'	and source=\''+ train_data_param1[idx]+'\' and os=\''+ train_data_param2[idx]+'\' group by 1) as conversion \
on conversion.offer_id=imp.offer_id \
join (select id,offer_name,payment,(payment/100.00) as rev from optimization.offers where item_type!=\'DeeplinkOffer\') as offer \
on offer.id=imp.offer_id;'

	print query_ecpm
	
	cursor.execute(query_ecpm)
	row = cursor.fetchall()
	if cursor.rowcount < 1 : 
		sys.exit()
		print "unsuccessful with eCPM query execution"
	print '#. eCPM offers', cursor.rowcount
	offerid_eCPM={}
	offerid_imp = {}
	offerid_conv = {}
	offerid_name = {}
	
	for row_ea in row:
		#offerid,offer-name,ecpm,conversion,impressions
		offerid_eCPM[row_ea[0]] = row_ea[4]
		offerid_imp[row_ea[0]] 	= row_ea[3]
		offerid_name[row_ea[0]] = row_ea[1]

	for key in offerid_name.keys():
		cur_score = float(offerid_eCPM[key])*math.pow((1.0/3)*math.log10(offerid_imp[key]),2)
		fw_train_ecpm.write(key+"\t"+offerid_name[key]+"\t"+str(round(offerid_eCPM[key],4))+"\t"+str(offerid_imp[key])+"\t"+str(round(cur_score,4))+"\n")
		#fw_train_ecpm.write(row_ea[0]+"\t"+row_ea[1]+"\t"+str(row_ea[4])+"\t"+str(row_ea[2])+"\t"+str(row_ea[3])+"\n")

	fw_train_ecpm.close()
	idx+=1