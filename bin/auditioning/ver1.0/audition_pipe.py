#!/usr/bin/env python

# verion 2.0 ############
# 1. instead of using convertibility files as "ground-truth", compute eCPM from optimization table
# 2. anything available from training eCPM result, use its eCPM score
# 3. put last N-hours (as training window), currently, set it as '48' hours
# 4. As input to predict eCPM, all unique offer-id (from optimization.offers) - offerid (has been "clicked" in last 48 hours)
#############################################


import os
import sys
import datetime,time
from time import gmtime,strftime
import shutil
import decimal
from datetime import timedelta
import pyodbc
cnxn = pyodbc.connect("DSN=VerticaDSN;UID=dbadmin;PWD=TJ4ever!",ansi="False")
cursor = cnxn.cursor()


# LOAD OFFER-NAME,DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com"
DB_USER="tapjoy"
DB_PWD="xatrugAxu6"
OUTPUT_DIR="/ebs/audition/ver2.0/offers_create.txt"
sql_query="select id,name, price from offers order by created_at desc;"


query_str ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql=sql_query,OUTPUT=OUTPUT_DIR)
os.system(query_str)


OUTPUT_DIR2="/ebs/audition/ver2.0/difficult_offers.txt"
#sql_query2 = 'select p.id \
#			from \
#			(select id,partner_id,name,price,item_type, countries from offers where price > 0 and (countries =\'\' or countries like \'%US%\')) p \
#			left join \
#			(select id as partner_id, balance from partners where balance < 1000000) q\
#			on p.partner_id = q.partner_id;'

sql_query2 = 'select id from offers where price > 0 and (countries =\'\' or countries like \'%US%\')';

query_str2 ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql=sql_query2,OUTPUT=OUTPUT_DIR2)
os.system(query_str2)


work_dir = '/ebs/audition/ver2.0'

today = datetime.datetime.now().strftime('%Y-%m-%d')
today_hr = datetime.datetime.now().strftime('%Y-%m-%d-%H-%M')
today = today_hr[0:10] # 2012-07-10
cur_work_hr = today_hr[11:16] # 18-59

today_work_dir = work_dir+'/'+today
cur_work_dir = work_dir+'/'+today+'/'+cur_work_hr

if  not os.path.exists(today_work_dir):
	os.makedirs(today_work_dir)

if not os.path.exists(cur_work_dir):
	os.makedirs(cur_work_dir)

############ deprecated in version 2.0 #####################################
#if os.system('/usr/local/bin/python /ebs/audition/gather_convrt_sc.py')!=0:
#	sys.exit()
######################


predict_class = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']

print 'cur_work_dir',cur_work_hr
gen_ios_sql = 'select a.id, a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
gen_android_sql = 'select a.id,a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';                                                                                                                                                                   
tjm_ios_sql = 'select a.id,a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
tjm_android_sql = 'select a.id,a.bid,a.payment,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';


test_data_sql = [gen_ios_sql,gen_android_sql,tjm_ios_sql,tjm_android_sql]
train_data_param1 = ['offerwall','offerwall','tj_games','tj_games']
train_data_param2 = ['iOS','Android','iOS','Android']

idx =0


for ea_class in predict_class:
	if idx !=2:
		pass
	#today_hr = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S.0')
	min_timeline1 = datetime.datetime.now() + timedelta(hours=-48)

	window_hr =  min_timeline1.strftime('%Y-%m-%d %H:%M:%S.0') #'2012-07-10 12:00:00.0'

	query_cpm = 'select imp.offer_id,s.offer_name,imp.impressions,conv.conversions,conv.spend,conv.spend*1000/imp.impressions as eCPM \
	from (select offer_id, count(*) as impressions from optimization.offerwall_views where time > \
	\''+ window_hr+'\'and source=\''+train_data_param1[idx]+ '\' and platform=\''+train_data_param2[idx]+'\' group by 1) as imp  \
    left join (select offer_id,count(*) as conversions, sum(advertiser_amount/-100.00) as spend from optimization.offerwall_actions where \
    converted_at > \''+window_hr+'\' and source=\''+train_data_param1[idx]+'\' and os=\''+train_data_param2[idx]+'\' group by 1) \
    as conv on imp.offer_id = conv.offer_id,optimization.offers s where conv.offer_id = s.id order by eCPM desc;'

#sample query result
#055315dd-1118-4dd4-a185-1782ce1241a8	Mobile Mouse (Remote/Trackpad for the iPad)	14636	8	26.000000000000000	1.776441650724241596064498496857065
#5abbcd51-e912-486d-b198-5d1dbc128b0f	Robinson for iPad	6954	42	12.600000000000000	1.811906816220880069025021570319241
	print query_cpm
	
	cursor.execute(query_cpm)
	row = cursor.fetchall()
	if cursor.rowcount < 1 : 
		sys.exit()
		print "unsuccessful with eCPM query execution"
	print '#. eCPM offers', cursor.rowcount
	offerid_eCPM={}
	offerid_imp = {}
	offerid_conv = {}
	for row_ea in row:
		offerid_eCPM[row_ea[0]]= '%.3f' % row_ea[5]
		#print row_ea[0], offerid_eCPM[row_ea[0]]
		offerid_imp[row_ea[0]] = row_ea[2]
		offerid_conv[row_ea[0]] = row_ea[3]
	
# since, it doesn't include offers (which don't have any conversion)
# so it needs to add other offers (which just "clicked"), but not apeared in offerid_eCPM dictionary
# currently, it will be used  as "black-list" (since it showed very poor performance in last N hours), will not appear in "auditioning" candidates

	min_timeline2 = datetime.datetime.now() + timedelta(hours=-120)
	window_hr2 = min_timeline2.strftime('%Y-%m-%d %H:%M:%S.0')
	query_clicked_id = 'select distinct a.offer_id from optimization.offerwall_views_agg a where source=\''+train_data_param1[idx] + '\' and \
	os=\''+train_data_param2[idx] +'\' and day > \''+ window_hr2+'\';'
	print query_clicked_id
	cursor.execute(query_clicked_id)
	offerid_blacklist = {}

	row_click = cursor.fetchall()
	for row_ea in row_click:
		if row_ea[0] not in offerid_eCPM: # offer => don't have any conversion, but clicked ==> "black-list"
			offerid_blacklist[row_ea[0]] = 1
	

# train-set: offerid_eCPM + offerid_blacklist
	
	fw_train = open('{cur_dir}/{cur_class}_train2.0.txt'.format(cur_dir=work_dir,cur_class=ea_class),'w')

	for key,value in offerid_eCPM.items():
		fw_train.write(key+","+value+"\n")
	for key,value in offerid_blacklist.items():
		fw_train.write(key+",-0.1\n") # -0.1 is just indicator for another class
	fw_train.close()
# test-set: all offers from optimization.offers - train-set
# audition delidery file: train-set (use 'eCPM' score) and predicted result of test-set

	cursor.execute(test_data_sql[idx])

	row  = cursor.fetchall()
	fw = open('{cur_dir}/{cur_class}_test2.0.new'.format(cur_dir=work_dir,cur_class=ea_class),'w')

	# for exist offfers, use eCPM as calculated
	fw_exist = open('{cur_dir}/{cur_class}_test2.0.exist'.format(cur_dir=work_dir,cur_class=ea_class),'w')
	for row_a in row:
    		#print row_a
    		if row_a[0] not in offerid_eCPM and row_a[0] not in offerid_blacklist: # all qualified offers - already consumed ones (in last N hours)
    			fw.write(row_a[0]+"\t"+str(row_a[1])+"\t"+str(row_a[2])+"\t"+row_a[3]+"\t"+str(row_a[4])+"\t"+str(row_a[5])+"\t"+row_a[6]+"\n")
    		else: #already exist offers
    		    fw_exist.write(row_a[0]+"\t"+str(row_a[1])+"\t"+str(row_a[2])+"\t"+row_a[3]+"\t"+str(row_a[4])+"\t"+str(row_a[5])+"\t"+row_a[6]+"\n")
	fw.close()
	fw_exist.close()
	
	# -- retrieve unique list of all partner-ids as a separate file
	output_partner_ids = '/ebs/audition/consolidate_partner_ids.txt'
	query_str0 ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql='select distinct partner_id from offers',OUTPUT=output_partner_ids)

	
	if os.system(query_str0)!=0:
        	sys.exit()

	if os.system('/usr/local/bin/python  {curdir}/test_data_build_arff.py {curdir}/{cur_class}_test2.0.new {curdir}/{cur_class}_test.arff'.format(curdir=work_dir,cur_class=ea_class))!=0:
		sys.exit()

	#print 'success:test_data_format',ea_class
	
	if os.system('/usr/local/bin/python {curdir}/train_data_build_arff.py {curdir}/{cur_class}_train2.0.txt {curdir}/{cur_class}_feature_vector.arff'.format(curdir=work_dir,cur_class=ea_class))!=0:
		sys.exit()
	#print 'success:train_data_format',ea_class
	#'''

	if os.system('java -cp {weka_dir}/weka.jar weka.filters.unsupervised.attribute.Remove -R {remove_col} -i {cur_dir}/{cur_class}_feature_vector.arff -o {cur_dir}/{cur_class}_feature_noid.arff'.format(weka_dir='/ebs/audition/weka-3-7-6',cur_dir=work_dir,remove_col='1',cur_class=ea_class))!=0:
		sys.exit()
	print 'success:weka_filter_train',ea_class

	if os.system('java -cp {weka_dir}/weka.jar weka.filters.unsupervised.attribute.Remove -R {remove_col} -i {cur_dir}/{cur_class}_test.arff -o {cur_dir}/{cur_class}_feature_test_noid.arff'.format(weka_dir='/ebs/audition/weka-3-7-6',cur_dir=work_dir,remove_col='1',cur_class=ea_class))!=0:
		sys.exit()
	print 'success:weka_filter_test',ea_class
	
	if os.system('java -cp {weka_dir}/weka.jar -Xmx{memory_size} weka.classifiers.{ml_algorithm} -I {num_trees} -K 0 -S 2 -split-percentage {train_ratio} -t {cur_dir}/{cur_class}_feature_noid.arff'.format(weka_dir='/ebs/audition/weka-3-7-6',memory_size='3036m',ml_algorithm='trees.RandomForest',num_trees='40', train_ratio='66',cur_dir=work_dir,cur_class=ea_class))!=0:
		sys.exit()

	if os.system('java -cp {weka_dir}/weka.jar -Xmx{memory_size} weka.classifiers.{ml_algorithm} -I {num_trees} -K 0 -S 2 -t {cur_dir}/{cur_class}_feature_noid.arff -T {cur_dir}/{cur_class}_feature_test_noid.arff -p {class_index} > {output}'.format(weka_dir='/ebs/audition/weka-3-7-6',memory_size='3036m',ml_algorithm='trees.RandomForest',num_trees='40',cur_dir=work_dir,cur_class=ea_class,class_index='7', output=work_dir+'/'+ea_class+'_pred.txt'))!=0:
		sys.exit()
	
	#python prediction_result.py /ebs/audition/ver2.0/tjm_ios_pred.txt [1] 
	#/ebs/audition/ver2.0/tjm_ios_train2.0.txt [2]
	#/ebs/audition/ver2.0/tjm_ios_test2.0.new [3]
	#/ebs/audition/ver2.0/tjm_ios_test2.0.exist [4]
	# /ebs/audition/ver2.0/tjm_ios_audition_predict [5]
	# tjm_ios_audition_predict [6]
	# /ebs/audition/ver2.0/tjm_ios_audition_predict_new [7]
    # 
	#print '/usr/local/bin/python {cur_dir}/predict_result.py {cur_dir}/{cur_class}_pred.txt {convert_dir}/{cur_class}_train2.0.txt \
	#	  {cur_dir}/{cur_class}_test2.0.new {cur_dir}/{cur_class}_test2.0.exist {cur_dir}/{cur_class}_audition_predict \
	#	  {cur_class}_audition_predict {cur_dir}/{cur_class}_audition_predict_new.format(cur_dir='+work_dir+',cur_class='+ea_class
	
	
#python /ebs/audition/ver2.0/predict_result_ver2.0.py /ebs/audition/ver2.0/tjm_ios_pred.txt /ebs/audition/ver2.0/tjm_ios_train2.0.txt /ebs/audition/ver2.0/tjm_ios_test2.0.new /ebs/audition/ver2.0/tjm_ios_test2.0.exist /ebs/audition/ver2.0/tjm_ios_audition_predict tjm_ios_audition_predict /ebs/audition/ver2.0/tjm_ios_audition_predict_new /ebs/audition/ver2.0/offers_create.txt
	if os.system('/usr/local/bin/python {cur_dir}/predict_result_ver2.0.py {cur_dir}/{cur_class}_pred.txt {cur_dir}/{cur_class}_train2.0.txt\
		 {cur_dir}/{cur_class}_test2.0.new {cur_dir}/{cur_class}_test2.0.exist {cur_dir}/{cur_class}_audition_predict \
		 {cur_class}_audition_predict {cur_dir}/{cur_class}_audition_predict_new {offer_name_file}'.format(cur_dir=work_dir,cur_class=ea_class,offer_name_file=OUTPUT_DIR))!=0:
		sys.exit()

	src_file = work_dir+'/'+ea_class+'_audition_predict'
	dst_file = cur_work_dir+'/'+ea_class+'_audition_predict'
	shutil.copy (src_file, dst_file)
	src_file2 = work_dir+'/'+ea_class+'_audition_predict_new'
	dst_file2 = cur_work_dir+'/'+ea_class+'_audition_predict_new'
	shutil.copy (src_file2, dst_file2)

	idx = idx + 1

#if os.system('/usr/local/python /ebs/audition/new_debug_result.py 101.1.iOS /ebs/audition/tjm_iOS_audition_predict '+cur_work_dir+'/tjm_iOS_audition_predict_report')!=0:
#		sys.exit()




