Bid Discount Package
====================

Overview
--------
Bid discount is a Python package and standalone program that calculates the discounted 
bid, as well as the ranking score using the discounted bid.

Installation
-----------

Installation is easy, just keep the whole directory *bid_discount*, which is also a Python module.

### Execute it as a standalone program

    ./discount_main.py -I 'indir' -O 'outdir'

Or
    python discount_main.py -I 'indir' -O 'outdir'

Use 
  * '-I' to specify the input directory, where the audition predicting output files are.
  * '-O' to specify the output directory, where the discounting output files will be put into.

Complete help information is below:
    Usage: discount_main.py [options]
    
    Calculate the discounted bid.
    
    Options:
      -h, --help            show this help message and exit
      -v, --verbose         turn on verbose mode.
      -I DIR, --indir=DIR   specify the input directory [default: /home/yzhou/work
                            shop/tapjoyoptimization/auditioning/bid_discount]
      -O DIR, --outdir=DIR  specify the output directory [default: /home/yzhou/wor
                            kshop/tapjoyoptimization/auditioning/bid_discount]
      -p PATTERN, --pattern=PATTERN
                            specify the input files
      -s SUFFIX, --suffix=SUFFIX
                            specify the output file suffix [default: .discount]
      -q Q, --defaultQ=Q    specify default Q value [default: 0.5]

### Execute it as a part of other Python program

    from bid_discount import discount_main
    discount_main.discount_main(indir, outdir, pattern, default_q, suffix)

By default, the last three parameters have default values, and the *discount_main* function is defined as:

    def discount_main(indir, outdir, pattern = '*_audition_predict', default_q = 0.5, suffix = '.discount'):



Data
----

### Inputs

    gen_iOS_audition_predict
    gen_Android_audition_predict
    tjm_iOS_audition_predict
    tjm_Android_audition_predict
  
The input files is determined by the file pattern specified as commandline argument or a parameter for function *discount_main*.

### Outputs

Three types of files are generated during the execution.

  * Discounted ranking file: the discounted ranking file, an extended version of the audition output, containing more columns
  * Discounted bid file (@discount): contains the calculating result of discounted **bid**.
  * Error file (@error): contains the list of offer ids that have no discount information matched.
  * Schema file (@schema): contains the schema of discounted ranking file.

The output files by default settings are:

    gen_iOS_audition_predict.discount
    gen_iOS_audition_predict.discount@discount
    gen_iOS_audition_predict.discount@error
    gen_iOS_audition_predict.discount@schema
    gen_Android_audition_predict.discount
    gen_Android_audition_predict.discount@discount
    gen_Android_audition_predict.discount@error
    gen_Android_audition_predict.discount@schema
    tjm_iOS_audition_predict.discount
    tjm_iOS_audition_predict.discount@discount
    tjm_iOS_audition_predict.discount@error
    tjm_iOS_audition_predict.discount@schema
    tjm_Android_audition_predict.discount
    tjm_Android_audition_predict.discount@discount
    tjm_Android_audition_predict.discount@error
    tjm_Android_audition_predict.discount@schema
    
### Schemas

**Schema for discount bid file**
    
    offer_id    # Original column from auditioning output
    dummy       # Original column from auditioning output
    bid         # Original column from auditioning output
    score       # Original column from auditioning output
    rank        # Rank with original score
    bid_star    # Discounted bid
    score_star  # Score calculated with the discounted bid
    rank_star   # Rank with the score_star

**Schema for @discount file**

    offer_id    # Offer id
    payment     # Payment
    bid_star    # Bid* between [payment, bid], caculated using Q
    bid         # Bid
    Q           # The offer quality

**Schema for @error file**
    
    offer_id
    
Design
------

### Modules

    + bid_discount
    | - dataprep        # data preparation module, mainly for vertica queries.
    | - discount_main   # contains the main module entry function *discount_main* and a standalone program.
    | - discount        # implements the main bid discount logic without dependency with current auditioning implementation.
    | - offer_q         # implements the calculating logic for offer qualities.

### Workflow

    dataprep module (Vertica)                           discount module                                     discount_main module                            Auditioning output files
    -------------------------                           ---------------                                     --------------------                            ------------------------
    
    1.1 optimization.offers                ---\
                                               \                                                                                                            4.1 gen_iOS_audition_predict
                                                == >    3.1 discount_bid(default_q = 0.5)             == >  5.1 discount_main()                     < ==    4.2 gen_Android_audition_predict
                                               /        Calculate discounted bids for all offers                  ^                                         4.3 tjm_iOS_audition_predict
    1.2 eCPM                               ---/                                                                   |                                         4.4 tjm_Android_audition_predict
    optimization.offerwall_views_agg,                                                                       5.2 main()  
    optimization.offerwall_actions,                                                                             ./discount_main.py -I 'indir' -O 'outdir'
    optimization.offers
                                                            ^                                                     ||
                                                            |                                                     ||
                                                            |                                                     \/
                                                        offer_q module                                      Output files
                                                        --------------                                      ------------
                                                        2.1 q_percentile                                    6.1 gen_iOS_audition_predict.discount
                                                            OR                                              6.2 gen_Android_audition_predict.discount
                                                        2.2 q_extreme                                       6.3 tjm_iOS_audition_predict.discount
                                                                                                            6.4 tjm_Android_audition_predict.discount

### Non-standard Python library

[Numpy](http://www.numpy.org) is a non-standard Python library used in this package for its non-in-place sorting function.
