#!/usr/bin/env python


#read self-test result, compute ratio of each classifier
#(w_price,w_payment,w_type,w_rating,w_partner)
#read original classifier scores (of each offer) from price,payment,type
#compute normalized classifier scores
#compute linear-combination scores (w_price*norm_price_classifier+w_payment*norm_payment_classifier+...)
#if an offer exist in rating, (+ w_rating*norm_rating_classifier)
#if an offer exist in partner-id (+ w_partner*norm_rating_classifier)

import sys
domain_name_list = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']
feature_name_list =['price','payment','rating','type']

main_dir = sys.argv[1] #/ebs/audition/adaboost/
cur_work_dir = sys.argv[2] #/ebs/audition/adaboost/2012-10-27/0035/
num_class = 3
from collections import defaultdict
#each classification result
#gen_iOS_type
#gen_iOS_rating
#gen_iOS_partner
#gen_iOS_price
#gen_iOS_payment
#example)
#d3eadb1a-ac53-47df-85b2-b097a5aef0bf,1 (new),1,0.0811745201448,2,0.0387570701785,3,0.0491767154427
#d3eadb1a-ac53-47df-85b2-b097a5aef0bf,1 (new),1,1.06204883935,2,0.728922278241,3,0.731445223688
#a8c7c613-0f60-41fa-890c-e39842b99286,0 (old),1,0.675082418927,2,0.44907813881,3,1.30269802352


#example of partner)
#28239536-44dd-417f-942d-8247b6da0e84,1,0.633333333333,30
#28239536-44dd-417f-942d-8247b6da0e84,2,0.333333333333,30
#28239536-44dd-417f-942d-8247b6da0e84,3,0.0333333333333,30

#self-evaluation result
#gen_iOS_price_self_validation
#gen_iOS_type_self_validation
#gen_iOS_rating_self_validation
#gen_iOS_payment_self_validation

#8c7c613-0f60-41fa-890c-e39842b99286,0.675082418927,0.44907813881,1.30269802352,0.278171305135,0.185045038173,0.536783656691,predict_class3,perform_class3,correct,-1.0
#60ca5e65-4cb9-4d4c-b684-605967ac2935,1.06204883935,0.728922278241,0.731445223688,0.421044227303,0.288977781468,0.289977991229,predict_class1,perform_class2,incorrect,0.421044227303

def dictionary_value_sum(input_dict):
	overall_val_sum = 0
	for ea_val in input_dict.values():
		overall_val_sum +=ea_val
	return overall_val_sum
def print_dictionary(input_dict):
	for key,val in input_dict.items():
		print key,val
for ea_domain in domain_name_list:
	classifier_weight = {} #price,0.343#payment,0.234
	norm_classifier_weight = {}
	#read each feature's error-margin rate & compute average values
	print '**************',ea_domain,'*********************'
	for ea_feature in feature_name_list:
		overall_instance = 0
		error_margin_sum = 0
		for ea_line in open(cur_work_dir+ea_domain+'_'+ea_feature+'_self_validation','r'):
			overall_instance +=1
			comp = ea_line.split(',')
			if comp[9] =='incorrect':
				error_margin_sum +=float(comp[10].strip())
		avg_error_margin = error_margin_sum/overall_instance
		classifier_weight[ea_feature] = 1.0/avg_error_margin #having higher score
	#print_dictionary(classifier_weight)
	print '-----------(normalized prob)-------------------------------------'
	#NORMALIZED_WIEGHT SCORE
	#norm_error_margin_sum = dictionary_value_sum(classifier_weight)
	#norm_error_margin_sum  = sum(classifier_weight)
 	#for ea_feature in classifier_weight.keys():
 	#	classifier_weight[ea_feature] = round(classifier_weight[ea_feature]/norm_error_margin_sum,3)
 	#print_dictionary(classifier_weight)
 	sum_each_classifier_weight = dictionary_value_sum(classifier_weight)
 	for ea_feature in classifier_weight.keys():
 		norm_classifier_weight[ea_feature] = round(classifier_weight[ea_feature]/sum_each_classifier_weight,3)
 	print_dictionary(norm_classifier_weight)
 	################################################################################

 	#norm_classifier_weight==> (price,0.3) (payment,0.3) (type,0.1) (rating,0.3)
 	#READ FROM CLASSIFIED RESULT OF EACH FEATURE (PRICE,PAYMENT,RATING,...)
 	offerid_predict={} #offerid of each classified results  ex) (124324,[0.3,0.2,0.4])
 	for ea_line in open(main_dir+ea_domain+'_'+ea_feature,'r'):#for ea_line in open('/ebs/audition/adaboost/'+ea_domain+'_'+ea_feature,'r'):
 		row = ea_line.split(',')
 		offerid = row[0]
 		#print ea_line
 	#SELECT ROW IF FLAG_NEW IS "1"
 		if row[1] !='1':
 			continue
 		init_idx = 2 #number of element as prefix-part (before predict-values of each class)
 		#use below way for handling any number of classes
 		offer_raw_predict_values=[]
 		offer_norm_predict = {} #offer_norm_predict = []
	 	for row_idx in range(init_idx+1,init_idx+init_idx*num_class,2): #only select predict-values (not predict class)
	 		offer_raw_predict_values.append(float(row[row_idx]))
	 	predict_val_sum = sum(offer_raw_predict_values)
	 	for i in range(num_class):
	 		offer_norm_predict[i] = norm_classifier_weight[ea_feature]*(offer_raw_predict_values[i]/predict_val_sum)
	  	#feature's impact weight * norm_predict_score (of each class)
	  	combined_feature_result = {}
	  	if offerid in offerid_predict: 
 			cur_feature_result = offerid_predict[offerid]
 			for idx in range(len(cur_feature_result)):
 				combined_feature_result[idx] = cur_feature_result[idx]+offer_norm_predict[idx]
 		#if current offer's predicted value already exist with previous feature, it adds predic values	
 		else:
 			combined_feature_result = offer_norm_predict
 		offerid_predict[offerid] = combined_feature_result #dictionary of dict 
 		#ex) 324234 ==> (0,0.23423),(1,0.5431),(2,0.7432)
		#print len(offerid_predict)
	f_partner = open(main_dir+ea_domain+'_partner','r')#f_partner = open('/ebs/audition/adaboost/'+ea_domain+'_partner','r')
	partner_classifer = defaultdict(list)
	for ea_line in f_partner:
		comp = ea_line.split(',')
		class_prob_pair= [comp[1],float(comp[2].strip())] #3456760f-183e-42c1-be29-6badbdc7940a,2,0.333
		partner_classifer[comp[0]].append(class_prob_pair)

	#prepare offferid-to-partnerid dictionary
	offerid_partnerid = {}
	f_feature = open(main_dir+ea_domain+'_feature.txt','r')#f_feature = open('/ebs/audition/adaboost/'+ea_domain+'_feature.txt','r')
	for ea_line in f_feature:
		comp = ea_line.split('\t')
		offerid_partnerid[comp[0]] = comp[6].strip()
	#scan through all offerid in 'offerid_predict' 
	#if offer has 'partner-id', then it combines with partner's performance values
	fw_output = open(cur_work_dir+ea_domain+'_predict_new','w')#fw_output = open('/ebs/audition/adaboost/'+ea_domain+'_predict_new','w')
	offerid_partner_prob = {}
	for offerid in offerid_predict.keys():
		
		if offerid in offerid_partnerid and offerid_partnerid[offerid] in partner_classifer:
			#partner_exist = 1
			#print 'partner'
			#print 'partner-id',offerid_partnerid[key]
			values = partner_classifer[offerid_partnerid[offerid]]
			#prepare partner's probability dictionary
			partner_prob = {}
			for ea_val in values:
				class_id = ea_val[0]
				class_prob= ea_val[1]
				class_idx = int(class_id) -1
				partner_prob[class_idx] = class_prob
				
			offerid_partner_prob[offerid] = partner_prob

			other_features_prob = offerid_predict[offerid] #ex) (0,0.23423),(1,0.5431),(2,0.7432)
			for class_indx in partner_prob.keys():
				consolidated_prob = other_features_prob[class_indx]*0.35+partner_prob[class_indx]*0.65
				#current combination ratio will be updated with real feedback-loop
				#update with consolidated_prob
				other_features_prob[class_indx]= consolidated_prob
			offerid_predict[offerid] = other_features_prob #updated with combined prob of partners
	#print_dictionary(offerid_partner_prob)
	for key,value in offerid_predict.items():
		output_list = []
		output_list.append(key)
		for class_idx,prob in value.items():
			output_list.append(str(class_idx+1)) #class starts with "1"
			output_list.append(str(prob))
		#appending partner's prob (for resulta analysis)	
		if key in offerid_partner_prob:
			for class_id, parter_prob in offerid_partner_prob[key].items():
				output_list.append(str(class_id+1))
				output_list.append(str(parter_prob))
		fw_output.write(','.join(output_list)+"\n")
