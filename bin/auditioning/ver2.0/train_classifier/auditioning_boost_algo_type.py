#!/usr/local/bin/python

from os import listdir
import json
from pprint import pprint
import sys
from collections import defaultdict
import datetime,time
from time import gmtime,strftime
import math
import pyodbc
import os


work_dir = sys.argv[1]

def class_assign(data_val,domain):
	data_class={}
	if domain=='gen_iOS':
		for ea_data in data_val.keys():
			if data_val[ea_data] > 10.0:
				data_class[ea_data] = 1
			elif data_val[ea_data] > 1.:
				data_class[ea_data] = 2
			else:
				data_class[ea_data] = 3			
	elif domain=='gen_Android':
		for ea_data in data_val.keys():
			if data_val[ea_data] > 10.0:
				data_class[ea_data] = 1
			elif data_val[ea_data] > 1.0:
				data_class[ea_data] = 2
			else:
				data_class[ea_data] = 3			
	elif domain=='tjm_iOS':
		for ea_data in data_val.keys():
			if data_val[ea_data] > 10.0:
				data_class[ea_data] = 1
			elif data_val[ea_data] > 1.0:
				data_class[ea_data] = 2
			else:
				data_class[ea_data] = 3			
	elif domain=='tjm_Android':
		for ea_data in data_val.keys():
			if data_val[ea_data] > 10.0:
				data_class[ea_data] = 1
			elif data_val[ea_data] > 1.0:
				data_class[ea_data] = 2
			else:
				data_class[ea_data] = 3			
	return data_class

# compute error-sum with given params (data_prob,threhold,)
#def comp_error_sum(data_featureVal,data_class,data_prob,operation,threshold):
def comp_error_sum(data_featureVal,data_class,data_prob,norm_value):
	error_sum = 0.0;
	for ea_data in data_class.keys():
		if (data_featureVal[ea_data] == norm_value) and data_class[ea_data] != 1:
			error_sum = error_sum + data_prob[ea_data]
		elif (data_featureVal[ea_data] !=norm_value) and data_class[ea_data] ==1:
			error_sum = error_sum + data_prob[ea_data]
			
	return error_sum

# weak classifier
def find_best_classifier(data_class,data_prob,data_nominal_feature):
	#it is unit-function, AdaBoost call this function iteratively
	#data_class: training data with its known class
	#data_prob: probability of each data (updated while running Ada-boost algorithm)
	#data_featureVal: data with its feature value (currently, we use a single dimension feature as data-point)
	#Get featureVal distribution

	feature_point_list =[]
	feature_num ={}

	for key in data_nominal_feature:
		if data_class[key] == 1:
			if data_nominal_feature[key] in feature_num:
				feature_num[data_nominal_feature[key]] = feature_num[data_nominal_feature[key]] +1
			else:
				feature_num[data_nominal_feature[key]] = 1
	
	for value in data_nominal_feature.values():
		if value not in feature_point_list:
			feature_point_list.append(value)
	#for key in feature_num:
    #		print key,feature_num[key]
	feature_point_list = sorted(feature_point_list)
	best_hypothesis_val = 0.0
	best_hypothesis_op = ''
    	best_error = 1.0
	

	for i in range(len(feature_point_list)):
		#print 'price feature', feature_point_list[i]
		cur_error_sum = comp_error_sum(data_nominal_feature,data_class,data_prob,feature_point_list[i])
		if cur_error_sum < best_error:
			best_error= cur_error_sum 
			best_hypothesis_val = feature_point_list[i]
		
	return best_error,best_hypothesis_val

#def update_data_prob(alpha,data_class,data_featureVal,data_prob,best_error,best_hypothesis_op,best_hypothesis_val):
def update_data_prob(alpha,data_class,data_featureVal,data_prob,best_error,best_hypothesis_val):	
	data_prob_total = 0.0
	for ea_data in data_prob.keys():
		# check if current data's value is correct with current best hypothesis
		if (data_featureVal[ea_data] == best_hypothesis_val and data_class[ea_data] == 1) or (data_featureVal[ea_data] != best_hypothesis_val and data_class[ea_data] == -1):
			data_prob[ea_data] = data_prob[ea_data]*math.exp(-1.0*alpha) # correct data
			data_prob_total = data_prob_total + data_prob[ea_data]
		else:
			data_prob[ea_data] = data_prob[ea_data]*math.exp(1.0*alpha) # incorrect data
			data_prob_total = data_prob_total + data_prob[ea_data]

	# normalize prob
	#print 'data_prob_total',data_prob_total
	for ea_data in data_prob.keys():
		data_prob[ea_data] = data_prob[ea_data]*1.0/data_prob_total
		#print ea_data, data_prob[ea_data]
	return data_prob


DB_USER="tapjoy"
DB_PWD="xatrugAxu6"
OUTPUT_DIR=work_dir+"offers_data.txt"

#get_app_offer_rating = 'select distinct item_id,bid,payment,daily_budget,overall_budget,item_type,price,partner_id from offers;'

#query_str ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql=get_app_offer_rating,OUTPUT=OUTPUT_DIR)
#if os.system(query_str)!=0:
#	sys.exit()

offerid_type ={}
f = open(OUTPUT_DIR,'r')
f.readline()

for ea_line in f:
	comp = ea_line.split('\t')
	offerid_type[comp[0]] = comp[5].strip()


#training_data_list =['gen_iOS_convert_all','gen_Android_convert_all','tjm_iOS_convert_all','tjm_Android_convert_all']
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']

predict_domain = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']

num_class = 3
num_domain = 4
for idx in range(num_domain):
	data_val = {}
	exist_data_val ={}
	exist_type = {}
	for ea_line in open(work_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		data_val[comp[0]]= float(comp[4].strip())
	for ea_id in data_val.keys():
		if ea_id in offerid_type:
			exist_type[ea_id] = offerid_type[ea_id]
			exist_data_val[ea_id] = data_val[ea_id]

	data_class = class_assign(exist_data_val,predict_domain[idx])

	for class_idx in range(1,num_class+1): # class idx ==> 1,2,3
		cur_data_class = {}
		for ea_data in data_class.keys():
			if ea_data in exist_type:
				if data_class[ea_data]==class_idx:
					cur_data_class[ea_data] = 1
				else:
					cur_data_class[ea_data] = -1
		#print 'class_idx',class_idx		
		cur_train_size = len(cur_data_class.keys())
		data_prob = {}
		#print 'cur_train_size', cur_train_size
		for ea_id in cur_data_class.keys():
			data_prob[ea_id] = 1.0/cur_train_size

		#for ea_data in cur_data_class.keys():
		#	print price_feature[ea_data],data_class[ea_data],cur_data_class[ea_data]
		#print 'size of data_prob', len(data_prob.keys())
		#print 'size of data_feature', len(exist_type.keys())
		
		hypothesis_list =[]
		exist_hypothesis = []
		num_iter = 20;
		fw = open(work_dir+predict_domain[idx]+"_type_"+str(class_idx),'w')

		for i in range(num_iter):
			# re-compute data-probability
			best_error,best_hypothesis_val = find_best_classifier(cur_data_class,data_prob,exist_type)
			# update alpha and recompute data-probability
			alpha = 0.5*math.log((1-best_error)/best_error)
			#print 'alpha',alpha
			

			if str(best_hypothesis_val) not in exist_hypothesis:
				exist_hypothesis.append(str(best_hypothesis_val))
				hypothesis_list.append(str(alpha)+","+str(best_hypothesis_val))
				#print 'best_error', best_error
				#print alpha,'*',best_hypothesis_val
				if best_error == 0.0:
					break
			
				data_prob = update_data_prob(alpha,cur_data_class, exist_type,data_prob,best_error,best_hypothesis_val)
			#else:
			#	break;
		
		for ea_hypo in hypothesis_list:
			#print ea_hypo;	
			fw.write(ea_hypo+"\n")
		fw.close()