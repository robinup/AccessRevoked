#!/usr/bin/env python
import os
import sys

def make_partnerid_arff(list_data):
        cat_arff = "{"
        partner_ids = ','.join(list_data)
        cat_arff += partner_ids
        cat_arff += "}"
        return cat_arff

partner_id_list = []
f_partner = open('/ebs/audition/consolidate_partner_ids.txt','r')
f_partner.readline()

for ea_line in f_partner: 
        ea_partnerid = ea_line.strip();
        partner_id_list.append(ea_partnerid)

partnerid_set = set(partner_id_list)

fw = open(sys.argv[2],'w') #feature vector arff file

fw.write("@relation test_offer_predict_feature\n")
fw.write("@attribute offer_id string\n")
#fw.write("@attribute created_at string\n")
#fw.write("@attribute updated_at string\n")

# really used feature parts (below)
fw.write("@attribute bid real\n")
fw.write("@attribute payment real\n")
fw.write("@attribute offer_type {EmailOffer,OfferpalOffer,RatingOffer,ReengagementOffer,VideoOffer,DeeplinkOffer,ActionOffer,App,GenericOffer,SurveyOffer}\n")
fw.write("@attribute price real \n")
fw.write("@attribute partner_balance real\n")
fw.write("@attribute partner_id ")
fw.write(make_partnerid_arff(partnerid_set)+"\n")
fw.write("@attribute class {0,1,2,3,4,5,6,7}\n")
fw.write("@data\n")

#raw_test data file (columns in order) o.item_id, o.device_types, o.created_at, o.updated_at, o.bid, o.payment, o.item_type, o.price, p.balance, o.partner_id
f_raw_data  = open(sys.argv[1],'r')
f_raw_data.readline() # read out first column names
for ea_line2 in f_raw_data:
	new_line = ea_line2.replace('\t',',')
	new_line = new_line.replace('\n',',')
	new_line_add = new_line+'?\n'
        #print new_line_add

	fw.write(new_line_add)
fw.write('\n')

