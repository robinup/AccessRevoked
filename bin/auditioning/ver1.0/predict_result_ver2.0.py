#!/usr/bin/evn python
#offerwall_ios_test.txt
#item_id bid     payment item_type       price   balance partner_id
#9b83ba4e-5fac-4b9f-8f6c-23f1c0483397    10      10      App     0       0       2d40f8a6-f05a-44f2-8bde-16cddf9a7c26
#3905fe87a-5402-490e-b418-fd22ef3f1177    10      10      App     0       0       67c803c2-76b6-4035-ba5e-f547b836effc
#44a12195-2322-4c31-952a-3584769badf6    10      10      App     0       0       67c803c2-76b6-4035-ba5e-f547b836effc
#5d4b1287-6d98-496a-a759-8c4560ac79ca    10      10      App     0       0       18f83f2f-2923-476e-ba00-6babbf4df38a
#ios_gen_pred.txt
#
#
#=== Predictions on test data ===
#
# inst#     actual  predicted error prediction
#     1        1:?        1:1       0.557
#     2        1:?        1:1       0.557
#     3        1:?        1:1       0.557


## version 2.0 prediction result file #####################################################
## predicit result only on test data (all valid offers - offers appears in last N hours)
## use train data's ecpm as its score
## final result : train_data's ecpm + test data's predicted ecpm value
## #########################################################################################



from collections import defaultdict
import sys
import os
from operator import itemgetter

# e.g. python prediction_result.py /ebs/audition/ver2.0/tjm_iOS_pred.txt (raw prediction file) /ebs/audition/ver2.0/tjm_iOS_train2.0.txt
#_audition_predict

instance_predict = {}
#f_pred = open('/ebs/optimz/gen_ios_pred.txt','r')

f_pred = open(sys.argv[1],'r')

# read dummy line
f_pred.readline()
f_pred.readline()
f_pred.readline()
f_pred.readline()
f_pred.readline()

for ea_line in f_pred:
	comp = ea_line.split()
	if len(comp)==4:
		predict_comp = comp[2].split(':')
		instance_predict[int(comp[0])]=predict_comp[0]

def comp_class(data):
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

# compute average values of each class
offer_convert = defaultdict(list)
offerclass_ecpm = defaultdict(list)
offer_ecpm = {}
class_avg_ecpm = {}
#for ea_line in open('//ebs/audition/ver2.0/tjm_iOS_train2.0.txt','r'):

for ea_line in open(sys.argv[2],'r'):
	comp = ea_line.split(',')
	offerclass_ecpm[comp_class(float(comp[1]))].append(float(comp[1]))
	offer_ecpm[comp[0]] = comp[1] #offerid,eCPM
for key,value in offerclass_ecpm.items():
	valsum =0
        for ea_val in value:
        	valsum += ea_val
        #print key,value
        #        print key,valsum/len(value)
                class_avg_ecpm[key] = valsum/len(value)

#for k,v in class_avg_convert.items():
#	print k,v

idx =1;
#item_id bid     payment item_type       price   balance partner_id
#9b83ba4e-5fac-4b9f-8f6c-23f1c0483397    10      10      App     0       0       2d40f8a6-f05a-44f2-8bde-16cddf9a7c26
#905fe87a-5402-490e-b418-fd22ef3f1177    10      10      App     0       0       67c803c2-76b6-4035-ba5e-f547b836effc

#python prediction_result.py /ebs/audition/ver2.0/tjm_iOS_pred.txt [1] 
#/ebs/audition/ver2.0/tjm_iOS_train2.0.txt [2]
#/ebs/audition/ver2.0/tjm_iOS_test2.0.new [3]
#/ebs/audition/ver2.0/tjm_iOS_test2.0.exist [4]
# /ebs/audition/ver2.0/tjm_ios_audition_predict [5]
# tjm_ios_audition_predict [6]
# /ebs/audition/ver2.0/tjm_ios_audition_new [7]


f_test_file = open(sys.argv[3],'r') #/ebs/audition/ver2.0/tjm_iOS_test2.0.new
f_test_file.readline()
#fw_combine = open('/ebs/audition/ver2.0/tjm_ios_audition_predict','w')
fw_combine = open(sys.argv[5],'w')

fw_new = open(sys.argv[7],'w')
fw_new_dict ={}
#fw_new = open('/ebs/audition/ver2.0/tjm_ios_audition_predict_new','w')
f_offer_meta = open(sys.argv[8],'r')
offerid_price = {}
offerid_name = {}

for ea_line in f_offer_meta:
	comp = ea_line.split('\t')
	offerid_name[comp[0]] = comp[1]
	offerid_price[comp[0]] = comp[2].strip()

DB_USER="tapjoy"
DB_PWD="xatrugAxu6"
OUTPUT_DIR="/ebs/audition/ver2.0/difficult_offers.txt"
sql_query = 'select p.id \
			from \
			(select id,partner_id,name,price,item_type, countries from offers where price > 0 and (countries =\'\' or countries like \'%US%\')) p \
			left join \
			(select id as partner_id, balance from partners where balance < 1000000) q\
			on p.partner_id = q.partner_id;'


query_str ="mysql -h{DB_HOST} -u{DB_USER} -p{DB_PWD} tapjoy_db -e\"{sql}\" > {OUTPUT}".format(DB_HOST="tapjoy-db-rds-replica2.cck8zbm50hdd.us-east-1.rds.amazonaws.com",DB_USER="tapjoy",DB_PWD="xatrugAxu6",sql=sql_query,OUTPUT=OUTPUT_DIR)
os.system(query_str)

difficult_offers ={}

f_diff_offers = open('/ebs/audition/ver2.0/difficult_offers.txt','r')

f_diff_offers.readline()

for ea_line in f_diff_offers:
	difficult_offers[ea_line.strip()] = 1;

for ea_line2 in f_test_file:
	comp  = ea_line2.split()
	offer_id = comp[0]
	bid = comp[1]
	predict_class = instance_predict[idx]
	item_type = comp[3] 
	price = int(comp[4])
	if item_type == 'App': # filter out other item-types (just for now!)
		#print 'predict_class',predict_class
		#predict_convert = class_avg_convert[int(predict_class)]
		predict_ecpm = class_avg_ecpm[int(predict_class)]
		#fw.write(offer_id+","+str(predict_ecpm)+","+bid+","+str((1+int(bid)/50.0)*predict_convert/(price+1.0))+"\n")
		if str(predict_ecpm) == '-0.1' or (str(predict_ecpm) < '0.3' and  offer_id in difficult_offers):
			continue
		else:
			fw_combine.write(offer_id+","+str(predict_ecpm)+","+bid+","+str(predict_ecpm)+"\n")
			fw_new_dict[offer_id+","+ offerid_name[offer_id]+","+offerid_price[offer_id]+","+str(predict_ecpm)+","+bid+","+str(predict_ecpm)] = predict_ecpm 
	idx = idx+1

# sort by score and write output
for key,value in sorted(fw_new_dict.iteritems(), key=itemgetter(1), reverse=True):
	fw_new.write(str(key)+"\n")

fw_new.close()
f_exist_file = open(sys.argv[4],'r') # /ebs/audition/ver2.0/tjm_iOS_test2.0.exist

# put filtering logic for false positive
# if eCPM < threshold (0.3)
#    && price >0 && country include 'US' && partner's balance < 10,000$
# then, filter out!
# if eCPM == -0.1 => filter out!



for ea_line in f_exist_file:
	comp = ea_line.split()
	offer_id = comp[0].strip()
	bid = comp[1]
	cur_ecpm = offer_ecpm[offer_id].strip()
	
	if cur_ecpm == '-0.1' or (cur_ecpm < '0.3' and  offer_id in difficult_offers):
		continue
	else:	
		fw_combine.write(offer_id+","+str(cur_ecpm)+","+bid+","+str(cur_ecpm)+"\n")

fw_combine.close()


# deliver to s3 bucket
if os.path.getsize(sys.argv[5]) > 100:
	print '/opt/s3sync/s3cmd.rb put tj-optimization-audition:'+sys.argv[6]+ ' '+sys.argv[5]
	os.system('/opt/s3sync/s3cmd.rb put tj-optimization-audition:'+sys.argv[6]+ ' '+sys.argv[5])

