import os
import sys
import datetime,time
from time import gmtime,strftime
import shutil
import decimal
from datetime import timedelta
import glob
from os import listdir
from collections import defaultdict
import json
import pyodbc
cnxn = pyodbc.connect("DSN=VerticaDSN;UID=dbadmin;PWD=TJ4ever!",ansi="False")
cursor = cnxn.cursor()

opt_day_list = glob.glob('/ebs/data/opt/201*')
print datetime.datetime.now().strftime('%Y-%m-%d-%h')

argv1_list=['280.1.iOS...iphone','280.1.Android...android','280.0.Android...android','280.0.iOS...iphone']
argv2_list=['tjm_iOS_perform_all','tjm_Android_perform_all','gen_Android_perform_all','gen_iOS_perform_all']
num_class =4


offerid_timestamp  ={} # offerid, timestamp (first time)

window_hr = sys.argv[1] #'2012-07-08'
train_data_param1 = ['tj_games','offerwall','offerwall','tj_games']
train_data_param2 = ['iOS','iOS','Android','Android']

'''
1.for each 4 top-segments
2.open last two hour's folder
3.
'''


for indx in range(num_class):
	print argv1_list[indx]

	global_offer_convert = defaultdict(list)
	fw = open(argv2_list[indx],'w'); # OUTPUT FILE
	for ea_day_dir in opt_day_list:
		if ea_day_dir > '/ebs/data/opt/'+sys.argv[1]: #'2012-08-02':
			for ea_hour_dir in listdir(ea_day_dir):
				for ea_opt_file in listdir(ea_day_dir+"/"+ea_hour_dir):
					#if ea_opt_file =='101.1.Android...android':
					if ea_opt_file == argv1_list[indx]:
						print ea_day_dir+"/"+ea_hour_dir+":"+ea_opt_file		
						#json_data = open("101.0.Android...android","r")
						try:
							json_data = open(ea_day_dir+"/"+ea_hour_dir+"/"+ea_opt_file,"r")
							data = json.load(json_data)
							curdict_array={}
				
					#PUT CURRENT (HOUR) OFFER-ID WITH CONVERTIBILITY SCORE IN DICTIONARY
							for record in data['offers']:
									cur_rank_sc = float(record['rank_score'])
									cur_rank_sc = cur_rank_sc*1000
									if cur_rank_sc%5==0: # normally end with *.**5
										cur_ts = ea_day_dir+":"+ea_hour_dir
										if record['offer_id'] not in offerid_timestamp:
											print record['offer_id'],cur_rank_sc,cur_ts
											offerid_timestamp[record['offer_id']] = cur_ts

				    		
						except Exception,err:
							print err
							pass						

	query_offer_country ='select id,countries from optimization.offers;'
	cursor.execute(query_offer_country)
	row2 = cursor.fetchall()
	offerid_country={}
	for row_ea in row2:
		offerid_country[row_ea[0]] = row_ea[1]

	query_cpm = 'select imp.offer_id,s.offer_name,imp.impressions,conv.conversions,conv.spend,conv.spend*1000/imp.impressions as eCPM \
	from (select offer_id, count(*) as impressions from optimization.offerwall_views where time > \
	\''+ window_hr+'\'and source=\''+train_data_param1[indx]+ '\' and platform=\''+train_data_param2[indx]+'\' group by 1) as imp  \
    left join (select offer_id,count(*) as conversions, sum(advertiser_amount/-100.00) as spend from optimization.offerwall_actions where \
    converted_at > \''+window_hr+'\' and source=\''+train_data_param1[indx]+'\' and os=\''+train_data_param2[indx]+'\' group by 1) \
    as conv on imp.offer_id = conv.offer_id,optimization.offers s where conv.offer_id = s.id order by eCPM desc;'


	cursor.execute(query_cpm)
	row = cursor.fetchall()
	if cursor.rowcount < 1 : 
		sys.exit()
	for row_ea in row:
		if row_ea[0] in offerid_timestamp:
			fw.write(row_ea[0]+"\t"+offerid_country[row_ea[0]]+"\t"+row_ea[1]+"\t"+str(row_ea[2])+"\t"+str(row_ea[3])+"\t"+str(row_ea[4])+"\t"+str(row_ea[5])+"\n")
	fw.close()
