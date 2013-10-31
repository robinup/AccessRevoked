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
from operator import itemgetter

#combine_model_human_factor.py /ebs/audition/adaboost/ 4 3
work_dir = sys.argv[1] #/ebs/audition/adaboost/2012-10-27/0035
num_domain = int(sys.argv[2])#4
num_class = int(sys.argv[3]) #3
main_dir = sys.argv[4]
result_dir = main_dir +'current'
if  not os.path.exists(result_dir):
	os.makedirs(result_dir)


# LOAD OFFER-NAME,DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com"
OUTPUT_DIR=main_dir+"offers_aud_daily_budget.txt"
sql_query="select id,item_id,name,bid,price,daily_budget,created_at,updated_at,audition_factor from offers \
where user_enabled=true and tapjoy_enabled=true;"

query_str ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",
DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql=sql_query,OUTPUT=OUTPUT_DIR)
os.system(query_str)

def exp_value_comp(data_val):
	exp_value=0
	if data_val > 10.0:
		exp_value = 10.0
	elif data_val > 1.0:
		exp_value = 1.0
	else:
		exp_value = 0.01

	return exp_value

def class_value_comp(class_num):
	exp_value=0
	if class_num ==1:
		exp_value = 10.0
	elif class_num ==2:
		exp_value = 1.0
	else:
		exp_value = 0.01

	return exp_value

predict_domain = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']
#training_data_list =['gen_iOS_convert_all','gen_Android_convert_all','tjm_iOS_convert_all','tjm_Android_convert_all']
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']

#e707b5ff-f54c-4890-acb0-b5a32e066c9f    Photo Editor - Fotolr   5.5528  1999    6.7222
#a30cbed5-9505-494a-83e2-76dbe5f8798c    The King of Chess (Chess)       21.7391 23      4.479

#gen_iOS_predict_new
#aece96ad-707e-4efd-a2cd-a7db2e7f52a7,1,0.101329636622,2,0.0247101239857,3,0.146960239392
#29759901-50ad-400a-8c99-8311bba4ed8c,1,0.19414528628,2,0.133454490424,3,0.417950223296,1,0.277777777778,2,0.166666666667,3,0.555555555556


offerid_aud_factor = {}
offerid_dailybudget = {}
offerid_bid = {}
offerid_price = {}
offerid_name = {}
offer_meta = open(OUTPUT_DIR,'r')
offer_meta.readline()
for ea_line in offer_meta:
	comp = ea_line.split('\t')
	
	if len(comp)<3: 
		continue 
	#print comp[8]
	offerid_aud_factor[comp[0]] = int(comp[8].strip())
	offerid_dailybudget[comp[0]] = float(comp[5])
	offerid_bid[comp[0]] = float(comp[3])
	offerid_price[comp[0]] = float(comp[4])
	offerid_name[comp[0]] = comp[2]
for idx in range(num_domain):
	#COMPUTE FINAL (AUDITION+DAILY CAP) SCORE FOR OFFERS WHICH ARE KNOWN ITS HISTORICAL PERFORMANCE
	fw_raw = open(work_dir+predict_domain[idx]+'_raw','w')
	fw_final = open(work_dir+predict_domain[idx]+'_audition_predict','w')
	fw_final_cur = open(result_dir+'/'+predict_domain[idx]+'_audition_predict','w')
	daily_cap = 1.0
	offerid_final_sc = {}	
	fw_raw.write('offer,offer-name,new_offer(0/1),raw_sc,audition_factor,bid,price,daily_budget\n')
	for ea_line in open(main_dir+training_data_list[idx],'r'):
		human_factor = 1.0
		raw_output = []
		comp = ea_line.split('\t')
		cur_offer = comp[0]
		cur_raw_sc= exp_value_comp(float(comp[4].strip()))

		#PRINT-OUT RAW OUTPUT
		raw_output.append(cur_offer)
		#raw_output.append(offerid_name[cur_offer])
		raw_output.append(0) #new-offer
		raw_output.append(cur_raw_sc)
		#if cur_offer not in offerid_aud_factor:
		#	print cur_offer,'not in audtion factor'
		
		if cur_offer in offerid_aud_factor:
			cur_audfact = offerid_aud_factor[cur_offer]
			cur_bid = offerid_bid[cur_offer]
			cur_price = offerid_price[cur_offer]
			cur_dailybudget = offerid_dailybudget[cur_offer]
		
		#PRINT-OUT RAW OUTPUT
			raw_output.append(cur_audfact)
			raw_output.append(cur_bid)
			raw_output.append(cur_price)
			raw_output.append(cur_dailybudget)
		
		
			if cur_audfact==10: #only apply human factor when it is "10"
				human_factor = 100
			if cur_dailybudget < 1000 and cur_dailybudget >0 : #apply daily cap when it is smaller than 1000$ 
				daily_cap = (1.0/3)*math.log10(cur_dailybudget)

			final_sc = daily_cap*human_factor*cur_bid*(1.0/(1.0+int(cur_price)*2.0))*cur_raw_sc
			raw_output.append(round(final_sc,3))
			offerid_final_sc[cur_offer] = round(final_sc,3)
			fw_raw.write(','.join(str(ea) for ea in raw_output)+"\n")
	#print len(offerid_final_sc),'old offers'
	#COMPUTE FINAL (AUDITION+DAILY CAP) SCORE FOR OFFERS WHICH ARE KNOWN ITS HISTORICAL PERFORMANCE		
		
	for ea_line in open(work_dir+predict_domain[idx]+'_predict_new','r'):
		raw_output = []
		comp = ea_line.split(',')
		init_idx = 1
		human_factor = 1 

		predict_values = []
		for indx in range(init_idx+1,init_idx+2*num_class,2):
			predict_values.append(comp[indx])
		predicted_class = predict_values.index(max(predict_values))
		cur_raw_sc = class_value_comp(predicted_class)
		cur_offer = comp[0]
		
		#PRINT-OUT RAW OUTPUT
		raw_output.append(cur_offer)
		#raw_output.append(offerid_name[cur_offer])
		raw_output.append(1) #new-offer
		raw_output.append(cur_raw_sc)

		#offerid_final_sc[cur_offer] = cur_raw_sc
		if cur_offer in offerid_aud_factor:
			cur_audfact = offerid_aud_factor[cur_offer]
			cur_bid = offerid_bid[cur_offer]
			cur_price = offerid_price[cur_offer]
			cur_dailybudget = offerid_dailybudget[cur_offer]

			#PRINT-OUT RAW OUTPUT
			raw_output.append(cur_audfact)
			raw_output.append(cur_bid)
			raw_output.append(cur_price)
			raw_output.append(cur_dailybudget)

			
			if cur_audfact==10: #only apply human factor when it is "10"
				human_factor = 100 
			if cur_dailybudget < 1000 and cur_dailybudget >0 : #apply daily cap when it is smaller than 1000$ 
				daily_cap = (1.0/3)*math.log10(cur_dailybudget)
			
			final_sc = daily_cap*human_factor*cur_bid*(1.0/(1.0+int(cur_price)*2.0))*cur_raw_sc
			offerid_final_sc[cur_offer] = round(final_sc,3)
			raw_output.append(round(final_sc,3))
			fw_raw.write(','.join(str(ea) for ea in raw_output)+"\n")
	#print len(offerid_final_sc),'all offers'
	for key,value in sorted(offerid_final_sc.iteritems(), key=itemgetter(1), reverse=True):
		final_output = []
		final_output.append(key)
		final_output.append(value)
		final_output.append(offerid_bid[key])
		final_output.append(value)
		#fw_final.write(cur_offer+","+str(final_sc)+","+str(cur_bid)+","+str(cur_bid*final_sc*(1.0/(1.0+int(offerid_price[cur_offer])*10)))+"\n")

		fw_final.write(','.join(str(ea) for ea in final_output)+"\n")
		fw_final_cur.write(','.join(str(ea) for ea in final_output)+"\n")
	fw_final.close()
	fw_final_cur.close()
	cur_predict_file = work_dir+predict_domain[idx]+'_audition_predict'
	if os.path.getsize(cur_predict_file) > 100:
		print '/opt/s3sync/s3cmd.rb put tj-optimization-audition:'+predict_domain[idx]+'_audition_predict'+ ' '+cur_predict_file
		os.system('/opt/s3sync/s3cmd.rb put tj-optimization-audition:'+predict_domain[idx]+'_audition_predict'+ ' '+cur_predict_file)
