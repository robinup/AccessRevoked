#!/usr/bin/evn python

'''
gen_iOS_payment
gen_iOS_rating
gen_iOS_price
gen_iOS_type

477a096e-6e28-4238-8d1c-213b22b79dca,0,1,0.675082418927,2,0.44907813881,3,1.30269802352
ac36b4f4-bee7-4efb-acf6-8de51d8b8789,1,1,0.675082418927,2,0.44907813881,3,1.30269802352

d3eadb1a-ac53-47df-85b2-b097a5aef0bf,1,1,0.0239605962269,2,0.0352188970231,3,0.136527364891
934222bc-36a6-45e4-8eae-b90248a2f5d5,0,1,0,2,0.096327642962,3,0.190483297829
f1fadebc-a571-4d98-a1a0-0655011ef30f,1,1,0,2,0.096327642962,3,0.190483297829
'''
#/usr/local/bin/python test/self_validation_classifiers.py  /ebs/audition/adaboost/2012-12-29/03-52/ type /ebs/audition/adaboost/ 

'''


'''
import csv
import sys
import math
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

predict_domain = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']
training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']

data_val = {}
work_dir = sys.argv[1]
feature = sys.argv[2] #payment, type
main_dir = sys.argv[3]
num_class =3
num_domain = 4#num_domain = 4

overall_error_score = 0
for idx in range(num_domain):

	for ea_line in open(main_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		data_val[comp[0]]= float(comp[4].strip())

	offer_perform_class = class_assign(data_val,predict_domain[idx])

 	fw_output = open(work_dir+predict_domain[idx]+'_'+feature+'_self_validation','w')
	# read each class's predicted result per offer-id
	# compute error-margin (+/-)
	#with open('/ebs/audition/adaboost/'+predict_domain[idx]+'_'+feature, 'r') as csvfile:
	#	csv_reader = csv.reader(csvfile, delimiter=',')
	num_records = 0
	for ea_line in open(main_dir+predict_domain[idx]+'_'+feature,'r'):	
		row = ea_line.strip().split(',')
		#for row in csv_reader:
		if row[1]=='1': #it doesn't self-test with new offers
			#print "continue"
			continue
		#print "old one"
		offer_output = []
		offerid = str(row[0])
	  	#print offerid
	  	predict_correct = "incorrect"
	  	offer_output.append(offerid)
	  	offer_raw_predict_values = []
	  	offer_norm_predict_values = []
	  	error_margin = 1.0
	  	init_idx = 2 #number of element as prefix-part (before predict-values of each class)
	  	for row_idx in range(init_idx+1,init_idx+init_idx*num_class,2): #only select predict-values (not predict class)
	  		offer_raw_predict_values.append(float(row[row_idx]))
	  	#normalize raw predict scores of each class
	  	total_sum_predict = 0
	  	for ea_value in offer_raw_predict_values:
	  		offer_output.append(str(ea_value))
	  		total_sum_predict +=ea_value
	  	if total_sum_predict ==0:
	  		print 'exception:',ea_line
	  		continue
	  	for ea_value in offer_raw_predict_values:
	  		offer_norm_predict_values.append(ea_value*1.0/total_sum_predict)
	  		offer_output.append(str(ea_value*1.0/total_sum_predict))
	  	#print total_sum_predict
	  	#compute error-margin
	 	if offerid not in offer_perform_class:
	  		print "no performance data,but old offer"
	  	#	continue

	  	max_predict_val = max(offer_norm_predict_values)
	  	predict_class = offer_norm_predict_values.index(max_predict_val)+1 
	  	#since its index starts from "0", class_idx starts with "1"
	  	num_records +=1
	  	offer_output.append('predict_class'+str(predict_class))
	  	offer_output.append('perform_class'+str(offer_perform_class[offerid]))
	  	if predict_class == offer_perform_class[offerid]:
	  		predict_correct = "correct"
	  		#error_margin = max_predict_val
	  		#overall_error_score += error_margin
	  	else:
	  		error_margin = 1.0*max_predict_val
	  		overall_error_score += error_margin*(math.fabs(predict_class-offer_perform_class[offerid]))
	  	
	  	offer_output.append(predict_correct)
	  	offer_output.append(str(error_margin))
	  	fw_output.write(','.join(offer_output)+'\n')
	fw_output.close()
	print 'overall_error_margin',predict_domain[idx],overall_error_score,overall_error_score/num_records

	


















