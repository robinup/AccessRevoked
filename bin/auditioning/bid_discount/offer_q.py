#!/usr/bin/env python

'''
Algorithms calculating the offer quality with respect to different perspectives
'''

import numpy as np

def q_extreme(scores):
    '''
    Input:
        scores - array of unsorted scores
        
    0.7% of scores are expected out of the range between (q1 - 1.5 * iqr) and (q3 + 1.5 * iqr), if the distribution is near Gaussian.
    '''
    tmp = np.sort(scores)
    n = len(tmp)
    q1 = tmp[n / 4]
    q3 = tmp[3 * n / 4]
    iqr = q3 - q1
    
    upper = q3 + 1.5 * iqr
    lower = q1 - 1.5 * iqr
    
    func = lambda v: max(lower, 0) / upper if v <= lower else (1 if v >= upper else max(v, 0) / upper)
    
    return [func(s) for s in scores]
    
    

def q_percentile(scores):
    '''
    Input:
        scores - array of unsorted scores
    '''
    
    tmp = np.sort(scores)
    rank = dict([(s, 0) for s in scores])
    n = len(tmp)
    for i, s in enumerate(tmp):
        rank[s] = (i + 1.0) / n
    
    return [rank[s] for s in scores]
    
    
    
if __name__ == '__main__':
    import unittest
    
    class TestFixture(unittest.TestCase):
        def setUp(self):
            pass
        
        def tearDown(self):
            pass
        
        def test_q_extreme(self):
            scores = np.random.randn(10)
            print scores
            
            print q_extreme(scores)
        
        def test_q_percentile(self):
            scores = np.random.randn(10)
            print scores
            
            print q_percentile(scores)
        
        
    unittest.main()
