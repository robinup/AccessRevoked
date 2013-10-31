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

predict_class = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']


feature_vec = ['price_feature','overall_budget','daily_budget','partner_balance','payment']

DB_USER="tapjoy"
DB_PWD="xatrugAxu6"
work_dir = sys.argv[1]
OUTPUT_DIR = work_dir+'offers_data.txt'#OUTPUT_DIR="/ebs/audition/adaboost/offers_data.txt"
get_offer_meta = 'select distinct item_id,bid,payment,daily_budget,overall_budget,item_type,price,partner_id from offers;'

query_str ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql=get_offer_meta,OUTPUT=OUTPUT_DIR)

if os.system(query_str)!=0:
	sys.exit()

#gen_ios_sql = 'select a.id, a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
#gen_android_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%offerwall%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';                                                                                                                                                                   
#tjm_ios_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%iphone%\' or device_types like \'%ipad%\' or device_types like \'%itouch%\')';
#tjm_android_sql = 'select a.id,a.bid,a.payment,a.daily_budget,a.overall_budget,a.item_type,a.price,b.partner_balance,b.partner_id from optimization.offers a, optimization.offers_partners b where a.id=b.offer_id and (a.approved_sources like \'%tj_games%\' OR a.approved_sources=\'\') and (device_types like \'%android%\')';

#training_data_list =['gen_iOS_convert_all','gen_Android_convert_all','tjm_iOS_convert_all','tjm_Android_convert_all']
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']
train_data_param1 = ['offerwall','offerwall','tj_games','tj_games']
train_data_param2 = ['iOS','Android','iOS','Android']


# class threshold
# tjm_ios ==>  good: x > 2.0, m: 0.01<x<2, bad: x <0.01
# gen_ios ==>  good: x > 5.0, m: 0.1<x<5, bad: x<0.1
# tjm_android ==> good: x >5.0, m: 0.01<x<5, bad: x<0.01
# gen_android ==> good: x>5.0, m: 0.1<x<5, bad x<0.1

def class_assign(data_val,domain):
	data_class={}
	if domain=='gen_iOS':
		for ea_data in data_val.keys():
			if data_val[ea_data] > 10.0:
				data_class[ea_data] = 1
			elif data_val[ea_data] > 1.0:
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
def comp_error_sum(data_featureVal,data_class,data_prob,operation,threshold):
	error_sum = 0.0;
	if operation == 'eq':
		for ea_data in data_class.keys():
			#print 'ea_data',ea_data
			if (data_featureVal[ea_data] == threshold) and data_class[ea_data] != 1:
				error_sum = error_sum + data_prob[ea_data]
			elif (data_featureVal[ea_data] > threshold) and data_class[ea_data] ==1:
				error_sum = error_sum + data_prob[ea_data]
				#print 'ea_data',ea_data
				#print 'data_featureVal[ea_data]', data_featureVal[ea_data]	
	elif operation == 'lt':
		for ea_data in data_class.keys():
			if (data_featureVal[ea_data] < threshold) and data_class[ea_data] != 1:
				error_sum = error_sum + data_prob[ea_data]
			elif (data_featureVal[ea_data] > threshold) and data_class[ea_data] ==1:
				error_sum = error_sum + data_prob[ea_data]
	elif operation == 'gt':
		for ea_data in data_class.keys():
			if (data_featureVal[ea_data] > threshold) and data_class[ea_data] != 1:
				error_sum = error_sum + data_prob[ea_data]
			elif (data_featureVal[ea_data] < threshold) and data_class[ea_data] ==1:
				error_sum = error_sum + data_prob[ea_data]
	return error_sum

# weak classifier
def find_best_classifier(data_class,data_prob,data_featureVal):
	#it is unit-function, AdaBoost call this function iteratively
	#data_class: training data with its known class
	#data_prob: probability of each data (updated while running Ada-boost algorithm)
	#data_featureVal: data with its feature value (currently, we use a single dimension feature as data-point)
	#Get featureVal distribution

	feature_point_list =[]
	feature_num ={}
	for key in data_featureVal:
		if data_class[key] == 1:
			if data_featureVal[key] in feature_num:
				feature_num[data_featureVal[key]] = feature_num[data_featureVal[key]] +1
			else:
				feature_num[data_featureVal[key]] = 1
	
	for value in data_featureVal.values():
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
		if i == 0:
			#print 'initial_threshold', feature_point_list[0]
			
			cur_error_sum = comp_error_sum(data_featureVal,data_class,data_prob,'eq',feature_point_list[0])
			#print 'best_error',best_error,'cur_error',cur_error_sum,best_hypothesis_val,best_hypothesis_op
			if cur_error_sum < best_error:
				best_error= cur_error_sum 
				best_hypothesis_val = feature_point_list[i]
				best_hypothesis_op = 'eq' 
				
			cur_error_sum = comp_error_sum(data_featureVal,data_class,data_prob,'gt',feature_point_list[0])
			#print 'best_error',best_error,'cur_error',cur_error_sum,best_hypothesis_val,best_hypothesis_op
			if cur_error_sum < best_error:
				best_error= cur_error_sum 
				best_hypothesis_val = feature_point_list[i]
				best_hypothesis_op = 'gt' 
				
		else:
			cur_threshold = (feature_point_list[i-1] + feature_point_list[i])*1.0/2
			#print 'cur_threshold',cur_threshold
			cur_error_sum =  comp_error_sum(data_featureVal,data_class,data_prob,'lt',cur_threshold)
			#print 'best_error',best_error,'cur_error',cur_error_sum,best_hypothesis_val,best_hypothesis_op
			if cur_error_sum < best_error:
				best_error = cur_error_sum
				best_hypothesis_val = cur_threshold
				best_hypothesis_op = 'lt'

			cur_error_sum =  comp_error_sum(data_featureVal,data_class,data_prob,'gt',cur_threshold)	
			#print 'best_error',best_error,'cur_error',cur_error_sum,best_hypothesis_val,best_hypothesis_op

			if cur_error_sum < best_error:
				best_error = cur_error_sum
				best_hypothesis_val = cur_threshold
				best_hypothesis_op = 'gt'
			

	return best_error,best_hypothesis_op,best_hypothesis_val

def update_data_prob(alpha,data_class,data_featureVal,data_prob,best_error,best_hypothesis_op,best_hypothesis_val):
	
	data_prob_total = 0.0
	for ea_data in data_prob.keys():
		# check if current data's value is correct with current best hypothesis
		if best_hypothesis_op == 'eq':
			if (data_featureVal[ea_data] == best_hypothesis_val and data_class[ea_data] == 1) or (data_featureVal[ea_data] != best_hypothesis_val and data_class[ea_data] == -1):
				data_prob[ea_data] = data_prob[ea_data]*math.exp(-1.0*alpha) # correct data
				data_prob_total = data_prob_total + data_prob[ea_data]
			else:
				data_prob[ea_data] = data_prob[ea_data]*math.exp(1.0*alpha) # incorrect data
				data_prob_total = data_prob_total + data_prob[ea_data]

		elif best_hypothesis_op == 'lt':
			if (data_featureVal[ea_data] < best_hypothesis_val and data_class[ea_data] == 1) or (data_featureVal[ea_data] >= best_hypothesis_val and data_class[ea_data] == -1) :
				data_prob[ea_data] = data_prob[ea_data]*math.exp(-1.0*alpha) # correct data
				data_prob_total = data_prob_total + data_prob[ea_data]
			else:
				data_prob[ea_data] = data_prob[ea_data]*math.exp(1.0*alpha) # incorrect data
				data_prob_total = data_prob_total + data_prob[ea_data]
				
		elif best_hypothesis_op == 'gt':
			if (data_featureVal[ea_data] > best_hypothesis_val and data_class[ea_data] == 1) or (data_featureVal[ea_data] <= best_hypothesis_val and data_class[ea_data] == -1):
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

idx =0
num_iter = 10
for ea_class in predict_class:
	#print '##################',ea_class,'##################'
	#cursor.execute(test_data_sql[idx])
	price_feature={}
	overall_budget = {}
	daily_budget = {}
	partner_balance ={}
	payment = {}
	data_val = {}
	
	for ea_line in open(work_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		data_val[comp[0]]= float(comp[4].strip())
	data_class = class_assign(data_val,ea_class)

#select distinct item_id (0),bid (1) ,payment (2) ,daily_budget (3) ,overall_budget (4),item_type (5),price (6),partner_id (7) from offers

	#for ea_key in data_class:
	#	print ea_key,data_val[ea_key],data_class[ea_key]
	#row  = cursor.fetchall()
	cur_train_size = 0
	f = open(work_dir+'offers_data.txt','r')
	f.readline()
	for ea_line in f:
		comp  =ea_line.split('\t')
		if comp[0] in data_class: # only filter per domain from all available offers
			price_feature[comp[0]] = int(comp[6]) # price
			overall_budget[comp[0]] = int(comp[4]) # overall budget
			daily_budget[comp[0]] = int(comp[3]) # daily budget
			payment[comp[0]] = int(comp[2]) # payment
	# multi-class run bi-class * N (num.classes) times
	num_class = 3
	
	# 'PRICE' feature based boosting modeling ################################
	#print '====== price ========='
	for class_idx in range(1,num_class+1): # class idx ==> 1,2,3
		fw = open(work_dir+ea_class+"_price_"+str(class_idx),'w') # write current classifier output
		cur_data_class = {}
		for ea_data in data_class.keys():
			if ea_data in price_feature:
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
		#print 'size of data_feature', len(price_feature.keys())
		hypothesis_list =[]
		exist_hypothesis = []
		
		for i in range(num_iter):
			# re-compute data-probability
			best_error,best_hypothesis_op,best_hypothesis_val = find_best_classifier(cur_data_class,data_prob,price_feature)
			# update alpha and recompute data-probability
			alpha = 0.5*math.log((1-best_error)/best_error)
			#print 'alpha',alpha
			if best_hypothesis_op+str(best_hypothesis_val) not in exist_hypothesis:
				exist_hypothesis.append(best_hypothesis_op+str(best_hypothesis_val))
				hypothesis_list.append(str(alpha)+","+best_hypothesis_op+","+str(best_hypothesis_val))
				#print 'best_error', best_error
				#print alpha,'*',best_hypothesis_op,best_hypothesis_val
				if best_error == 0.0:
					break
			
				data_prob = update_data_prob(alpha,cur_data_class, price_feature,data_prob,best_error,best_hypothesis_op,best_hypothesis_val)
			
		for ea_hypo in hypothesis_list:
			#print ea_hypo;	
			fw.write(ea_hypo+"\n")
		fw.close()
    # 'OVERALL BUDGET' feature based boosting modeling ################################
	#print '======== overall budget ==========='
	for class_idx in range(1,num_class+1): # class idx ==> 1,2,3
		fw = open(work_dir+ea_class+"_overall_budget_"+str(class_idx),'w') # write current classifier output
		cur_data_class = {}
		for ea_data in data_class.keys():
			if ea_data in overall_budget:
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
		#print 'size of data_feature', len(overall_budget.keys())
		hypothesis_list =[]
		exist_hypothesis = []
		
		for i in range(num_iter):
			# re-compute data-probability
			best_error,best_hypothesis_op,best_hypothesis_val = find_best_classifier(cur_data_class,data_prob,overall_budget)
			# update alpha and recompute data-probability
			alpha = 0.5*math.log((1-best_error)/best_error)
			#print 'alpha',alpha
			if best_hypothesis_op+str(best_hypothesis_val) not in exist_hypothesis:
				exist_hypothesis.append(best_hypothesis_op+str(best_hypothesis_val))
				hypothesis_list.append(str(alpha)+","+best_hypothesis_op+","+str(best_hypothesis_val))
				#print 'best_error', best_error
				#print alpha,'*',best_hypothesis_op,best_hypothesis_val
				if best_error == 0.0:
					break
			
				data_prob = update_data_prob(alpha,cur_data_class, overall_budget,data_prob,best_error,best_hypothesis_op,best_hypothesis_val)
			
		for ea_hypo in hypothesis_list:
			#print ea_hypo;	
			fw.write(ea_hypo+"\n")
		fw.close()

	# 'DAILY BUDGET' feature based boosting modeling ################################
	#print '======== daily budget ==========='
	for class_idx in range(1,num_class+1): # class idx ==> 1,2,3
		fw = open(work_dir+ea_class+"_daily_budget_"+str(class_idx),'w') # write current classifier output
		cur_data_class = {}
		for ea_data in data_class.keys():
			if ea_data in daily_budget:
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
		#print 'size of data_feature', len(daily_budget.keys())
		hypothesis_list =[]
		exist_hypothesis = []
		
		for i in range(num_iter):
			# re-compute data-probability
			best_error,best_hypothesis_op,best_hypothesis_val = find_best_classifier(cur_data_class,data_prob,daily_budget)
			# update alpha and recompute data-probability
			alpha = 0.5*math.log((1-best_error)/best_error)
			#print 'alpha',alpha
			if best_hypothesis_op+str(best_hypothesis_val) not in exist_hypothesis:
				exist_hypothesis.append(best_hypothesis_op+str(best_hypothesis_val))
				hypothesis_list.append(str(alpha)+","+best_hypothesis_op+","+str(best_hypothesis_val))
				#print 'best_error', best_error
				#print alpha,'*',best_hypothesis_op,best_hypothesis_val
				if best_error == 0.0:
					break
			
				data_prob = update_data_prob(alpha,cur_data_class, daily_budget,data_prob,best_error,best_hypothesis_op,best_hypothesis_val)
			
		for ea_hypo in hypothesis_list:
			#print ea_hypo;	
			fw.write(ea_hypo+"\n")
		fw.close()
	
	# 'PAYMENT' feature based boosting modeling ################################
	#print '======== payment ==========='
	for class_idx in range(1,num_class+1): # class idx ==> 1,2,3
		fw = open(work_dir+ea_class+"_payment_"+str(class_idx),'w') # write current classifier output
		cur_data_class = {}
		for ea_data in data_class.keys():
			if ea_data in daily_budget:
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
		#print 'size of data_feature', len(partner_balance.keys())
		hypothesis_list =[]
		exist_hypothesis = []
		
		for i in range(num_iter):
			# re-compute data-probability
			best_error,best_hypothesis_op,best_hypothesis_val = find_best_classifier(cur_data_class,data_prob,payment)
			# update alpha and recompute data-probability
			alpha = 0.5*math.log((1-best_error)/best_error)
			#print 'alpha',alpha
			if best_hypothesis_op+str(best_hypothesis_val) not in exist_hypothesis:
				exist_hypothesis.append(best_hypothesis_op+str(best_hypothesis_val))
				hypothesis_list.append(str(alpha)+","+best_hypothesis_op+","+str(best_hypothesis_val))
				#print 'best_error', best_error
				#print alpha,'*',best_hypothesis_op,best_hypothesis_val
				if best_error == 0.0:
					break
			
				data_prob = update_data_prob(alpha,cur_data_class,payment,data_prob,best_error,best_hypothesis_op,best_hypothesis_val)
			
		for ea_hypo in hypothesis_list:
			#print ea_hypo;	
			fw.write(ea_hypo+"\n")
		fw.close()

	idx = idx +1