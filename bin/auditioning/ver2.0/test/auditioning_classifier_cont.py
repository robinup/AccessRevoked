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
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']

feature = sys.argv[1] #'price', 'payment'
feature_idx = int(sys.argv[2]) #5 ==>comp[5], 1==>comp[1]
work_dir = sys.argv[3]
gen_ios_sql = 'select a.id, a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
gen_android_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';                                                                                                                                                                   
tjm_ios_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
tjm_android_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';

test_data_sql = [gen_ios_sql,gen_android_sql,tjm_ios_sql,tjm_android_sql]

def classifier_cont_func(alpha,op,val,x):
	
	if (op == 'eq' and x ==val) or (op == 'lt' and x < val) or (op == 'gt' and x > val):
		return alpha*1.0
	else:
		return 0

# read all test-offer's feature vectors

idx = 0
num_class = 3

for ea_domain in predict_domain:
	#classification output per domain
	#fw_output = open('/ebs/audition/adaboost/'+ea_domain+'_continuous.txt','w')
	fw_output = open(work_dir+ea_domain+'_'+feature,'w')
	train_data_val = {}
	#print ea_domain
	
	idx = idx +1
	test_data_set = {}
	# read from feature vector file
	offerid_newflag = {}
	fr_feature = open(work_dir+ea_domain+'_feature.txt','r')
	for ea_line in fr_feature:
		offer_feature = []
		comp = ea_line.split('\t')
		test_data_set[comp[0]] = float(comp[feature_idx])
		offerid_newflag[comp[0]] = comp[7].strip() # 0:old, 1:new
	# load all classifier of current domain (gen_iOS,gen_Android,...)
	overall_classifier = defaultdict(list)
  
  	for class_idx in range(1,num_class+1): # for each classes (1,2,3...)
		classifier_funcs = []
		#classifier_key = str(class_idx)+','+feature		
		classifier_key = class_idx
		for ea_func in open(work_dir+ea_domain+'_'+feature+'_'+str(class_idx),'r'): 
				comp = ea_func.split(',')
				alpha = float(comp[0])
				op = comp[1]
				val = float(comp[2].strip())
				overall_classifier[classifier_key].append([alpha,op,val]) 
				
	########################################################################
	
	for key,feature_val in test_data_set.items(): #key:offerid, value: a feature value
		cur_offer_features ={}
		class_total_prob = {} # accumulate all probability from each classifier (per same class) ex) 1:0.4, 2:0.4 3:0.2
		classfier_prob = {} #probability of each classifier, will be recorded for checking its prediction correctness 
		feature_prob = {} # sum of prob. of each class per feature
		curkey_output = []
		curkey_output.append(key) #key=> offer-id
		curkey_output.append(offerid_newflag[key]) #new_flag
		
		for ea_classifier,func_list in overall_classifier.items():
			sum_f_value = 0
			for ea_func in func_list:
				f_value = classifier_cont_func(ea_func[0],ea_func[1],ea_func[2],feature_val)
				sum_f_value = sum_f_value +f_value
			
			classfier_prob[ea_classifier] = sum_f_value;
		
		for key in sorted(classfier_prob.iterkeys()):
			curkey_output.append(str(key))
			curkey_output.append(str(classfier_prob[key]))
		fw_output.write(','.join(curkey_output))
		fw_output.write('\n')
	fw_output.close()