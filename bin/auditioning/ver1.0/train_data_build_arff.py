#!/usr/bin/env python
from sys import argv
import pyodbc
import os
cnxn = pyodbc.connect("DSN=VerticaDSN;UID=dbadmin;PWD=TJ4ever!",ansi="False")
cursor = cnxn.cursor()

# train-set: offerid_eCPM + offerid_blacklist



partnerid_list =[]
#e.g. /ebs/optimz/convertibility_all/gen_iOS_convert_all
convert_all_dic={}
ecpm_all_dic = {}
# read all offer-id,eCPM pair (consolidated)
for ea_line in open(argv[1],'r'): 
	comp = ea_line.split(',')
	#convert_all_dic[comp[0]] = float(comp[1].strip())
	ecpm_all_dic[comp[0]] = float(comp[1].strip())

fw = open(argv[2],"w") # output file

# w.r.t. eCPM, it will make more #. classes for making predict class more "discriminative"
# since in version 1.0 test, there are lots of offers belong to same class 2

def assign_class(data):
        class_num = 0;
        if data > 100:
                class_num =  7
        elif data > 50:
                class_num =  6
        elif data > 25:
               class_num  =  5
        elif data > 10:
                class_num = 4
        elif data > 5:
                class_num = 3
        elif data > 1: 
                class_num =  2   
        elif data > -0.01:
                class_num = 1
        else:
                class_num = 0
	return class_num

def make_cat_arff(list_data):
        cat_arff = "{"
        partner_ids = ','.join(list_data)
        cat_arff += partner_ids
        cat_arff += "}"
        return cat_arff

#row  = cursor.fetchall()
sql_str = 'select item_id,bid,payment,partner_id,item_type,price,partner_balance from optimization.offers_partners;'


#for row_a in row:
#	if row_a.item_id in convert_all_dic: # collect partner_id list (belong to current targeted offer_set)
#		partnerid_list.append(row_a.partner_id)
partner_id_list = []
f_partner = open('/ebs/audition/consolidate_partner_ids.txt','r')
f_partner.readline()
for ea_line in f_partner:
        ea_partnerid = ea_line.strip();
        partner_id_list.append(ea_partnerid)

partnerid_set = set(partner_id_list)


# head write
fw.write("@relation offer_predict_feature\n")
fw.write("@attribute offer_id string\n")
fw.write("@attribute bid real\n")
fw.write("@attribute payment real\n")
fw.write("@attribute offer_type {EmailOffer,OfferpalOffer,RatingOffer,ReengagementOffer,VideoOffer,DeeplinkOffer,ActionOffer,App,GenericOffer,SurveyOffer}\n")
fw.write("@attribute price real \n")
fw.write("@attribute partner_balance real\n")
fw.write("@attribute partner_id ")
fw.write(make_cat_arff(partnerid_set)+"\n")
fw.write("@attribute class {0,1,2,3,4,5,6,7}\n")
fw.write("@data\n")
cursor2 = cnxn.cursor()
cursor2.execute(sql_str)
row = cursor2.fetchall()

for row_b in row:
	if row_b.item_id in ecpm_all_dic:
#		print row_b.item_id,convert_all_dic[row_b.item_id],assign_class(convert_all_dic[row_b.item_id])
		fw.write(row_b.item_id+","+str(row_b.bid)+","+str(row_b.payment)+","+str(row_b.item_type)+","+str(row_b.price)+","+str(row_b.partner_balance)+","+str(row_b.partner_id)+","+str(assign_class(ecpm_all_dic[row_b.item_id]))+"\n")
fw.close()
