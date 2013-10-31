#!/usr/local/bin/python

from os import listdir
import json
from pprint import pprint
import sys
from collections import defaultdict
import datetime,time
from time import gmtime,strftime
import math
import os
import pyodbc
cnxn = pyodbc.connect("DSN=VerticaDSN;UID=dbadmin;PWD=TJ4ever!",ansi="False")
cursor = cnxn.cursor()


#########################################################
# 1. READ TRAINING FILE, LOAD ALL ACTIVE OFFERS
# 2. LOAD TRAINED CLASSIFIERS
# 3. FOR EACH TEST-OFFER (NOT APPEARED IN TRAINING SET)
# 4. 	RECORD OF EACH CLASSIFIER'S RAW SCORE
# 5. 	COMBINE LOCAL SCORE FOR FINAL GLOBAL DECISION
#########################################################


predict_domain = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']
#training_data_list =['gen_iOS_convert_all','gen_Android_convert_all','tjm_iOS_convert_all','tjm_Android_convert_all']
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']


gen_ios_sql = 'select a.id, a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
gen_android_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';                                                                                                                                                                   
tjm_ios_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
tjm_android_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';

test_data_sql = [gen_ios_sql,gen_android_sql,tjm_ios_sql,tjm_android_sql]
work_dir = sys.argv[1]

# read all test-offer's feature vectors

idx = 0

for ea_domain in predict_domain:
	#classification output per domain
	#fw_output = open('/ebs/audition/adaboost/'+ea_domain+'_continuous.txt','w')
	train_data_val = {}
	#print ea_domain
	# load all training data of current domain
	for ea_line in open(work_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		train_data_val[comp[0]]= float(comp[4].strip())

	# load all valid offers as of now
	cursor.execute(test_data_sql[idx])
	#print test_data_sql[idx]
	row  = cursor.fetchall()
	#print len(row)
	if len(row) < 100:
		print ea_domain,"data fetching error"
		continue
		
	#test_data_set = defaultdict(list)
	test_data_set = {}
	fw_feature =  open(work_dir+ea_domain+'_feature.txt','w')
	
	for ea_row in row:
		flag_new =0 #if new offer ==> flag_new =1
		offer_feature=[]
		#if ea_row[5]!='DeeplinkOffer':
		#	continue
		if ea_row[0] not in train_data_val:
			flag_new = 1
		if ea_row[5]=='DeeplinkOffer':
			continue
		feature_vec_output = []
		feature_vec_output.append(ea_row[0]) #offer-id (0)
		feature_vec_output.append(str(ea_row[2]))#payment (1)
		feature_vec_output.append(str(ea_row[3]))#daily_budget (2)
		feature_vec_output.append(str(ea_row[4]))#overall_budget (3)
		feature_vec_output.append(ea_row[5])#item_type (4)
		feature_vec_output.append(str(ea_row[6]))#price (5)
		feature_vec_output.append(ea_row[7])#parterid (6)
		feature_vec_output.append(str(flag_new))#flag_new (7)
		fw_feature.write('\t'.join(feature_vec_output)+"\n")
	fw_feature.close()
	#fw_feature2.close()
	idx = idx +1

	