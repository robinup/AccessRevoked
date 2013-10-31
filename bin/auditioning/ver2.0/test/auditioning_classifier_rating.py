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


predict_domain = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']

work_dir =sys.argv[1]
def classifier_cont_func(alpha,op,val,x):
	
	if (op == 'eq' and x ==val) or (op == 'lt' and x < val) or (op == 'gt' and x > val):
		return alpha*1.0
	else:
		return 0

# read all test-offer's feature vectors

idx = 0
num_class = 3
feature ='rating'
for ea_domain in predict_domain:
	#classification output per domain
	fw_output = open(work_dir+ea_domain+'_rating','w')
	train_data_val = {}
	offer_newflag = {}
	#print ea_domain
	# load all training data of current domain
	for ea_line in open(work_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		train_data_val[comp[0]]= float(comp[4].strip())

	#test_data_set = defaultdict(list)
 	test_data_set = {}
	idx = idx + 1
	# load rating feature vector
	f_rating = open(work_dir+"app_offers_rating.txt",'r') #offerid,rating
	f_rating.readline()
	offerid_rating = {}
	for ea_line in f_rating:
		#offer_feature = []
		comp = ea_line.split('\t')
		offerid_rating[comp[0]] = float(comp[1].strip())
		
	f_feature = open(work_dir+ea_domain+'_feature.txt','r')
	for ea_line in f_feature:
		comp = ea_line.split('\t')

		if comp[0] not in offerid_rating:
			continue
		#if comp[0] not in train_data_val and comp[0] in offerid_rating: # offerid not in train-dataset && offerid is app-offer
		offer_newflag[comp[0]] = comp[7].strip()
		test_data_set[comp[0]] = float(offerid_rating[comp[0]])
	# load all classifier of current domain (gen_iOS,gen_Android,...)
		overall_classifier = defaultdict(list)
		for class_idx in range(1,num_class+1): # for each classes (1,2,3...)
			#print 'class_idx',class_idx
			classifier_funcs = []
			classifier_key = class_idx
				
			for ea_func in open(work_dir+ea_domain+'_'+feature+'_'+str(class_idx),'r'): # write current classifier output
				comp = ea_func.split(',')
				alpha = float(comp[0])
				op = comp[1]
				val = float(comp[2].strip())
			#classifier_funcs.append([alpha,op,val])	
				#print 'classifier_key',classifier_key
				#print 'alpha,op,val',alpha,op,val
				overall_classifier[classifier_key].append([alpha,op,val])
			
	########################################################################
	
	for key,feature_val in test_data_set.items(): #key:offerid, value: list of feature values
		cur_offer_features ={}
		class_total_prob = {} # accumulate all probability from each classifier (per same class) ex) 1:0.4, 2:0.4 3:0.2
		classfier_prob = {} #probability of each classifier, will be recorded for checking its prediction correctness 
		feature_prob = {} # sum of prob. of each class per feature
		#print 'offerid',key
		#fw_output.write(key+',')
		curkey_output = []
		curkey_output.append(key) #key=> offer-id
		#flag_new = 0 #if new offer==> flag_new =1, otherwise 0
		#if key not in train_data_val:
		#	flag_new = 1
		curkey_output.append(offer_newflag[key])
		
		for ea_classifier,func_list in overall_classifier.items():
			sum_f_value = 0
			
			for ea_func in func_list:
				f_value = classifier_cont_func(ea_func[0],ea_func[1],ea_func[2],feature_val)
				#print 'f_value',f_value
				sum_f_value = sum_f_value +f_value
			
			#print 'sum_f_value',sum_f_value
			
			classfier_prob[ea_classifier] = sum_f_value;
			
		
		for key in sorted(classfier_prob.iterkeys()):
			curkey_output.append(str(key))
			curkey_output.append(str(classfier_prob[key]))

		fw_output.write(','.join(curkey_output))
		fw_output.write('\n') 
	fw_output.close()