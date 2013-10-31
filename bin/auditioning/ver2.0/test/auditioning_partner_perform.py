#!/usr/local/bin/python

from os import listdir
import sys
from collections import defaultdict
import math
import os

work_dir = sys.argv[1]
#from collections import defaultdict
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

offerid_partner = {}
f = open(work_dir+'offers_data.txt','r')
f.readline() # read header
for ea_line in f:
	comp = ea_line.split('\t')
	offerid_partner[comp[0]] = comp[7].strip() # patner id

training_data_list =['gen_iOS_ecpm_new','gen_Android_ecpm_new','tjm_iOS_ecpm_new','tjm_Android_ecpm_new']
predict_domain = ['gen_iOS','gen_Android','tjm_iOS','tjm_Android']

num_class = 3
num_domain = 4

for idx in range(num_domain):
	data_val = {}
	exist_data_val ={}
	exist_partner = {}
	partnerid_offerlist = defaultdict(list)
	fw = open(work_dir+predict_domain[idx]+'_partner','w')
	for ea_line in open(work_dir+training_data_list[idx],'r'):
		comp = ea_line.split('\t')
		data_val[comp[0]]= float(comp[4].strip())
	
	for ea_id in data_val.keys():
		if ea_id in offerid_partner:
			#exist_partner[ea_id] = offerid_partner[ea_id]
			exist_data_val[ea_id] = data_val[ea_id]
			partnerid_offerlist[offerid_partner[ea_id]].append(ea_id)
	data_class = class_assign(exist_data_val,predict_domain[idx])

	for partnerid,offerlist in partnerid_offerlist.items():
	# for each partner, compute distribution of classes (performance class)
		dataclass_count = {}
		for ea_offerid in offerlist:
			if data_class[ea_offerid] in dataclass_count:
				dataclass_count[data_class[ea_offerid]] = dataclass_count[data_class[ea_offerid]]+1
			else:
				dataclass_count[data_class[ea_offerid]] = 1
		for ea_class in dataclass_count:
			#print partnerid,ea_class,dataclass_count[ea_class]*1.0/len(offerlist),len(offerlist) 
			fw.write(partnerid+','+str(ea_class)+','+str(dataclass_count[ea_class]*1.0/len(offerlist))+','+str(len(offerlist))	+'\n')
	fw.close()