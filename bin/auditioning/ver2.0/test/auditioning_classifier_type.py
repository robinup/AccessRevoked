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

feature = 'type'
work_dir = sys.argv[1]
def classifier_nominal_func(alpha,val,x):
	
	if x ==val:
		return alpha*1.0
	else:
		return 0

# read all test-offer's feature vectors

idx = 0
num_class = 3
for ea_domain in predict_domain:
	#classification output per domain
	fw_output = open(work_dir+ea_domain+'_type','w')
	train_data_val = {}
	offer_newflag = {}
	print ea_domain
	# load all training data of current domain
	for ea_line in open(work_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		train_data_val[comp[0]]= float(comp[4].strip())

	test_data_set = {}
	idx = idx + 1
	# load all valid offers as of now
	fr_feature = open(work_dir+ea_domain+'_feature.txt','r')

	for ea_line in fr_feature:
		comp = ea_line.split('\t')
		#test_data_set[comp[0]].append(comp[4]) # offer-type
		test_data_set[comp[0]] = comp[4]
		offer_newflag[comp[0]] = comp[7].strip()
	# load all classifier of current domain (gen_iOS,gen_Android,...)
		overall_classifier = defaultdict(list)
		for class_idx in range(1,num_class+1): # for each classes (1,2,3...)
			classifier_funcs = []
			classifier_key = class_idx 
			for ea_func in open(work_dir+ea_domain+'_'+feature+'_'+str(class_idx),'r'): 
				comp = ea_func.split(',')
				alpha = float(comp[0])
				#op = comp[1]
				#val = float(comp[2].strip())
				val = comp[1].strip()
				#classifier_funcs.append([alpha,op,val])	
				overall_classifier[classifier_key].append([alpha,val])
			
			#classifier_key = str(class_idx)+','+feature_vec[idx2]		
			#overall_classifier[classifier_key].append(classifier_funcs)
	########################################################################
	
	for key,feature_val in test_data_set.items(): #key:offerid, value: list of feature values
		cur_offer_features ={}
		class_total_prob = {} # accumulate all probability from each classifier (per same class) ex) 1:0.4, 2:0.4 3:0.2
		classfier_prob = {} #probability of each classifier, will be recorded for checking its prediction correctness 
		feature_prob = {} # sum of prob. of each class per feature
		#print 'offerid',key
		curkey_output = []
		curkey_output.append(key)
		curkey_output.append(offer_newflag[key])

		for ea_classifier,func_list in overall_classifier.items():
			sum_f_value = 0
			for ea_func in func_list:
				f_value = classifier_nominal_func(ea_func[0],ea_func[1],feature_val) #f_value = classifier_nominal_func(ea_func[0],ea_func[1],cur_offer_features[feat_name])				
				sum_f_value = sum_f_value +f_value
			classfier_prob[ea_classifier] = sum_f_value;
		
		for key in sorted(classfier_prob.iterkeys()):
			curkey_output.append(str(key))
			curkey_output.append(str(classfier_prob[key]))
		fw_output.write(','.join(curkey_output)+"\n")
	fw_output.close()
