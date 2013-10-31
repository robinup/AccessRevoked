#!/usr/bin/env python

import fnmatch
import logging
import os
import optparse

import discount

def sort_rank(data):
    return sorted(range(len(data)), key = data.__getitem__, reverse = True)

def discount_main(indir, outdir, pattern = '*_audition_predict', default_q = 0.5, suffix = '.discount'):
    infiles = [os.path.join(indir, f) for f in os.listdir(indir) if fnmatch.fnmatch(f, pattern)]
    infiles = [f for f in infiles if os.path.isfile(f)]
    
    dis_all = discount.discount_bid(default_q)
    
    for f in infiles:
        fn = os.path.basename(f)
        seg = '_'.join(fn.split('_')[0:2])
        dis = dis_all[seg]
        
        errlist = []
        
        ret = []
        
        logging.info('Reading file %s' % f)
        with open(f) as fp:
            for l in fp:
                l = l.strip()
                if not l:
                    continue
                offer_id, dummy, bid, score = l.split(',')
                
                if dis.has_key(offer_id):
                    payment, bid_star, bid2, q = dis.get(offer_id)
                    score_star = float(score) / float(bid) * bid_star
                else:
                    bid_star = bid
                    score_star = score
                    rank_star = 0
                    errlist.append(offer_id)
                    logging.error('Offer %s not found in discounted list.' % offer_id)
                
                ret.append([offer_id,   # 0.offer_id 
                            dummy,      # 1.dummy
                            bid,        # 2.bid
                            score,      # 3.score
                            0,          # 4.rank
                            bid_star,   # 5.bid_star
                            score_star, # 6.score star
                            0           # 7.rank_star
                            ])
        logging.info('Total %d records processed.' % len(ret))
                
        # score
        rank = 1
        for i in sort_rank([float(r[3]) for r in ret]):
            ret[i][4] = rank
            rank = rank + 1
            
        # score_star
        rank = 1
        for i in sort_rank([r[6] for r in ret]):
            ret[i][7] = rank
            rank = rank + 1
                
        outfile = '%s%s' % (os.path.join(outdir, fn), suffix)
        logging.info('Writing output file %s' % outfile)
        with open(outfile, 'w') as fp:
            for r in ret:
                fp.write(','.join([str(e) for e in r]))
                fp.write('\n')
        logging.info('Output done.')
        
        disfile = '%s%s@discount' % (os.path.join(outdir, fn), suffix)
        logging.info('Writing discount file %s' % disfile)
        with open(disfile, 'w') as fp:
            fp.write('offer_id\tpayment\tbid_star\tbid\tQ\n')
            for offer_id in dis:
                payment, bid_star, bid2, q = dis.get(offer_id)
                fp.write('\t'.join([str(e) for e in (offer_id, payment, bid_star, bid2, q)]))
                fp.write('\n')
        
        errfile = '%s%s@error' % (os.path.join(outdir, fn), suffix)
        logging.info('Writing error list %s' % errfile)
        with open(errfile, 'w') as fp:
            for l in errlist:
                fp.write(l)
                fp.write('\n')
        
        schemafile = '%s%s@schema' % (os.path.join(outdir, fn), suffix)
        logging.info('Writing schema file %s' % schemafile)
        with open(schemafile, 'w') as fp:
            fp.write('offer_id, dummy, bid, score, rank, bid_star, score_star, rank_star')
    

def main():
    
    parser = optparse.OptionParser(description = 'Calculate the discounted bid.')
    parser.add_option('-v', '--verbose', dest = 'verbose', default = False, 
                      action = 'store_true',
                      help = 'turn on verbose mode.')
    parser.add_option('-I', '--indir', dest = 'indir', metavar = 'DIR',
                      default = os.getcwd(),
                      help = 'specify the input directory [default: %default]')
    parser.add_option('-O', '--outdir', dest = 'outdir', metavar = 'DIR',
                      default = os.getcwd(),
                      help = 'specify the output directory [default: %default]')
    parser.add_option('-p', '--pattern', dest = 'pattern', metavar = '',
                      default = '*_audition_predict',
                      help = 'specify the input files ')
    parser.add_option('-s', '--suffix', dest = 'suffix', metavar = 'SUFFIX',
                      default = '.discount',
                      help = 'specify the output file suffix [default: %default]')
    parser.add_option('-q', '--defaultQ', dest = 'defaultQ', metavar = 'Q',
                      default = 0.5, type = float,
                      help = 'specify default Q value [default: %default]')
    opts, args = parser.parse_args()
    
    if opts.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
    
    indir = os.path.expandvars(os.path.expanduser(opts.indir))
    outdir = os.path.expandvars(os.path.expanduser(opts.outdir))
    
    discount_main(indir, outdir, opts.pattern, opts.defaultQ, opts.suffix)
    

if __name__ == '__main__':
    main()