#!/usr/bin/env python
import os
import sys
import datetime,time
from time import gmtime,strftime
import shutil
import decimal
from datetime import timedelta


#overall process of auditioning (ada-boost)

'''
1.train_classifier
prep_training_data_eCPM_new.py /ebs/audition/adaboost/ 30
auditioning_boost_algo_continuous.py /ebs/audition/adaboost/
auditioning_boost_algo_rating.py /ebs/audition/adaboost/
auditioning_boost_algo_type.py /ebs/audition/adaboost/

2.test
python prepare_test_continous_data.py /ebs/audition/adaboost/
python auditioning_classifier_cont.py price 5 /ebs/audition/adaboost/
python auditioning_classifier_cont.py payment 1 /ebs/audition/adaboost/
python auditioning_classifier_rating.py /ebs/audition/adaboost/
python auditioning_classifier_type.py /ebs/audition/adaboost/
python auditioning_partner_perform.py /ebs/audition/adaboost/
python self_validation_classifier.py cur_work_dir feature
python final_classification_result.py /ebs/audition/adaboost/ cur_work_dir
python combine_model_human_factor.py path 4(#.domain) 3(#.classes)
'''

if len(sys.argv) > 1:
    work_dir = sys.argv[1]
else:
    work_dir = '/ebs/audition/adaboost/'

#code_dir = '/ebs/audition/adaboost/dev/'
code_dir = '/ebs/audition/adaboost/'

today = datetime.datetime.now().strftime('%Y-%m-%d')
today_hr = datetime.datetime.now().strftime('%Y-%m-%d-%H-%M')
today = today_hr[0:10] # 2012-07-10
cur_work_hr = today_hr[11:16] # 18-59

today_work_dir = work_dir+today
cur_work_dir = work_dir+today+'/'+cur_work_hr +'/'
feature_set = ['price','payment','rating','type']
if  not os.path.exists(today_work_dir):
	os.makedirs(today_work_dir)

if not os.path.exists(cur_work_dir):
	os.makedirs(cur_work_dir)

if os.system('/usr/local/bin/python {code_dir}train_classifier/prep_training_data_eCPM_new.py {curdir} {lbw}'.format(code_dir=code_dir,curdir=work_dir,lbw=30))!=0:
		print 'error during prep_training_data_eCPM_new process'
		sys.exit()
print 'success of prep_training_data_eCPM_new'

if os.system('/usr/local/bin/python {code_dir}train_classifier/auditioning_boost_algo_continuous.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during auditioning_boost_algo_continuous process'
		sys.exit()
print 'success of auditioning_boost_algo_continuous'

if os.system('/usr/local/bin/python {code_dir}train_classifier/auditioning_boost_algo_rating.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during auditioning_boost_algo_rating process'
		sys.exit()
print 'success of auditioning_boost_algo_rating'

if os.system('/usr/local/bin/python {code_dir}train_classifier/auditioning_boost_algo_type.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during auditioning_boost_algo_type process'
		sys.exit()
print 'success of auditioning_boost_algo_type'

if os.system('/usr/local/bin/python {code_dir}test/prepare_test_continuous_data.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during prepare_test_continous_data process'
		sys.exit()
print 'success of prepare_test_continous_data'

if os.system('/usr/local/bin/python {code_dir}test/auditioning_classifier_cont.py {feature} {idx} {curdir}'.format(code_dir=code_dir,feature="price",idx="5",curdir=work_dir))!=0:
		print 'error during auditioning_classifier_cont price feature process'
		sys.exit()
print 'success of auditioning_classifier_cont price'
if os.system('/usr/local/bin/python {code_dir}test/auditioning_classifier_cont.py {feature} {idx} {curdir}'.format(code_dir=code_dir,feature="payment",idx="1",curdir=work_dir))!=0:
		print 'error during auditioning_classifier_cont payment feature process'
		sys.exit()
print 'success of auditioning_classifier_cont payment'
if os.system('/usr/local/bin/python {code_dir}test/auditioning_classifier_rating.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during auditioning_classifier_rating process'
		sys.exit()
print 'success of auditioning_classifier_rating'
if os.system('/usr/local/bin/python {code_dir}test/auditioning_classifier_type.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during auditioning_classifier_type process'
		sys.exit()
print 'success of auditioning_classifier_type'
if os.system('/usr/local/bin/python {code_dir}test/auditioning_partner_perform.py {curdir}'.format(code_dir=code_dir,curdir=work_dir))!=0:
		print 'error during auditioning_partner_perform process'
		sys.exit()
print 'success of auditioning_partner_perform'
for ea_feature in feature_set:
	if os.system('/usr/local/bin/python {code_dir}test/self_validation_classifiers.py {cur_work_dir} {feature} {curdir}'.format(code_dir=code_dir,feature=ea_feature,curdir=work_dir,cur_work_dir=cur_work_dir))!=0:
		print 'error during self_validation_classifier process'
		sys.exit()
print 'success of self_validation_classifier'
if os.system('/usr/local/bin/python {code_dir}test/final_classification_result.py {maindir} {cur_work_dir}'.format(code_dir=code_dir,maindir=work_dir,cur_work_dir=cur_work_dir))!=0:
		print 'error during final_classification_result process'
		sys.exit()
print 'success of final_classification_result'
if os.system('/usr/local/bin/python {code_dir}test/combine_model_human_factor.py {cur_work_dir} {num_domain} {num_class} {maindir}'.format(code_dir=code_dir,maindir=work_dir,cur_work_dir=cur_work_dir,num_domain=4,num_class=3))!=0:
		print 'error during combine_model_human_factor process'
		sys.exit()
print 'success of combine_model_human_factor'


