package com.tapjoy.opt.logistic_regression;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.math.*;

import org.javatuples.Triplet;
import org.json.simple.parser.ParseException;

public class ContextPredictor {
        private String _source = null;
        private String _platform = null;
        private String _modelPath = null;
        private String _summaryPath = null;
        private String _metricsPath = null;
        private String _offlinePath = null;
        private ArrayList<Double> _rankWeights = null;
        
        private int _maxRank = 25;

        private TimeZone _tz;
        
        private DataPackage _dataPackage = null;
        
        private double _intercept = 0.0;
        
        private double _weekdayCoef = 0.0;
        private double _daytimeCoef = 0.0;
        
        private double _exchangeRateMean = 0.0;
        private double _exchangeRateStd = 1.0;
        private double _exchangeRateCoef = 0.0;
        
        private double _exchangeRateLogMean = 0.0;
        private double _exchangeRateLogStd = 1.0;
        private double _exchangeRateLogCoef = 0.0;

        private double _offerwallRankCVRMean = 0.0;
        private double _offerwallRankCVRStd = 1.0;
        private double _offerwallRankCVRCoef = 0.0;
        
        private double _deviceTypeCVRMean = 0.0;
        private double _deviceTypeCVRStd = 1.0;
        private double _deviceTypeCVRCoef = 0.0;
        
        private double _countryCVRMean = 0.0;
        private double _countryCVRStd = 1.0;
        private double _countryCVRCoef = 0.0;
        
        private double _cityCVRMean = 0.0;
        private double _cityCVRStd = 1.0;
        private double _cityCVRCoef = 0.0;
        
        private double _languageCVRMean = 0.0;
        private double _languageCVRStd = 1.0;
        private double _languageCVRCoef = 0.0;
        
        private double _hourCVRMean = 0.0;
        private double _hourCVRStd = 1.0;
        private double _hourCVRCoef = 0.0;
        
        private double _dowCVRMean = 0.0;
        private double _dowCVRStd = 1.0;
        private double _dowCVRCoef = 0.0;
        
        private double _weekdayCVRMean = 0.0;
        private double _weekdayCVRStd = 1.0;
        private double _weekdayCVRCoef = 0.0;
        
        public ContextPredictor(String source, String platform, String modelPath, String summaryPath, String metricsPath, String offlinePath) throws IOException, ParseException
        {
                _source = source;
                _platform = platform;
                _modelPath = modelPath;
                _summaryPath = summaryPath;
                _metricsPath = metricsPath;
                _offlinePath = offlinePath;
                
                _tz = TimeZone.getTimeZone("UTC");
                
                _rankWeights = new ArrayList<Double>(_maxRank);
                for (int i = 0; i < _maxRank; ++i) {
                        _rankWeights.add(1.0);
                }
                
                reloadDataPackage();
        }
        
        public ArrayList<Double> getRankWeights()
        {
                return _rankWeights;
        }
        
        public void setRankWeights(ArrayList<Double> weights) 
        {
                if (weights.size() != _maxRank) {
                        throw new RuntimeException(String.format("The length of weights are not %d.", _maxRank));
                }
                
                _rankWeights = weights;
        }
        public void reloadDataPackage() throws IOException, ParseException
        {
                DataPackage dataPackage = new DataPackage(_source, _platform, _modelPath, _summaryPath, _metricsPath, _offlinePath);
                
                synchronized(this) {
                        _dataPackage = dataPackage;
                        _intercept = dataPackage.getCoefficient("intercept");
                        _weekdayCoef = dataPackage.getCoefficient("context^weekday");
                        _daytimeCoef = dataPackage.getCoefficient("context^daytime");
                        _exchangeRateCoef = dataPackage.getCoefficient("context^exchange_rate");
                        _exchangeRateLogCoef = dataPackage.getCoefficient("context^exchange_rate_log");
                        _offerwallRankCVRCoef = dataPackage.getCoefficient("context_metrics^offerwall_rank_cvr");
                        _deviceTypeCVRCoef = dataPackage.getCoefficient("context_metrics^device_type_cvr");
                        _countryCVRCoef = dataPackage.getCoefficient("context_metrics^country_cvr");
                        _cityCVRCoef = dataPackage.getCoefficient("context_metrics^city_cvr");
                        _languageCVRCoef = dataPackage.getCoefficient("context_metrics^language_cvr");
                        _hourCVRCoef = dataPackage.getCoefficient("context_metrics^hour_cvr");
                        _dowCVRCoef = dataPackage.getCoefficient("context_metrics^dow_cvr");
                        _weekdayCVRCoef = dataPackage.getCoefficient("context_metrics^weekday_cvr");
                        
                        
                        Triplet<Double, Double, Boolean> summary;
                        
                        summary = dataPackage.getSummary("context^exchange_rate");
                        _exchangeRateMean = summary.getValue0();
                        _exchangeRateStd = summary.getValue1();
                        
                        summary = dataPackage.getSummary("context^exchange_rate_log");
                        _exchangeRateLogMean = summary.getValue0();
                        _exchangeRateLogStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^offerwall_rank_cvr");
                        _offerwallRankCVRMean = summary.getValue0();
                        _offerwallRankCVRStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^device_type_cvr");
                        _deviceTypeCVRMean = summary.getValue0();
                        _deviceTypeCVRStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^country_cvr");
                        _countryCVRMean = summary.getValue0();
                        _countryCVRStd = summary.getValue1();
                        
                        summary = dataPackage.getSummary("context_metrics^city_cvr");
                        _cityCVRMean = summary.getValue0();
                        _cityCVRStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^language_cvr");
                        _languageCVRMean = summary.getValue0();
                        _languageCVRStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^hour_cvr");
                        _hourCVRMean = summary.getValue0();
                        _hourCVRStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^dow_cvr");
                        _dowCVRMean = summary.getValue0();
                        _dowCVRStd = summary.getValue1();

                        summary = dataPackage.getSummary("context_metrics^weekday_cvr");
                        _weekdayCVRMean = summary.getValue0();
                        _weekdayCVRStd = summary.getValue1();

                }
        }
        
        public DataPackage getDataPackage()
        {
                synchronized(this) {
                        return _dataPackage;
                }
        }
        
        
        public double predict(
                        String source, 
                        String platform, 
                        String deviceType, 
                        String country,
                        String city,
                        String language,
                        int offerwallRankStart,
                        int offerwallRankEnd,
                        boolean weekday,
                        String dow,
                        String hour,
                        boolean daytime,
                        String ampm,
                        double exchangeRate,
                        String offerId,
                        String appId,
                        String userId)
        {
                if (offerwallRankStart < 1 || offerwallRankStart > offerwallRankEnd || offerwallRankEnd > _maxRank) {
                        throw new RuntimeException(String.format("Invalid offerwall ranks [%d, %d]", offerwallRankStart, offerwallRankEnd));
                }
                
                DataPackage dataPackage = getDataPackage();
                
                double exchangeRateLog = (exchangeRate > 1) ? Math.floor(Math.log(exchangeRate)) : 0.0;
                Triplet<Double, Double, Boolean> summary = null;
                
                double poly = 0.0;
                
                double op1 = 0.0;
                
                op1 += dataPackage.getCoefficient("context^source=" + source);
                op1 += dataPackage.getCoefficient("context^platform=" + platform);
                op1 += dataPackage.getCoefficient("context^device_type=" + deviceType);
                op1 += dataPackage.getCoefficient("context^country=" + country);
                op1 += dataPackage.getCoefficient("context^city=" + city);
                op1 += dataPackage.getCoefficient("context^language=" + language);
                op1 += (weekday ? 1 : 0) * _weekdayCoef;
                op1 += dataPackage.getCoefficient("context^dow=" + dow);
                op1 += dataPackage.getCoefficient("context^hour=" + hour);
                op1 += (daytime ? 1 : 0) * _daytimeCoef;
                op1 += dataPackage.getCoefficient("context^ampm=" + ampm);
                
                op1 += (exchangeRate - _exchangeRateMean) / _exchangeRateStd * _exchangeRateCoef;                
                op1 += (exchangeRateLog - _exchangeRateLogMean) / _exchangeRateLogStd * _exchangeRateLogCoef;
                
                
                double op2 = 0.0;
                
                op2 += (dataPackage.getMetrics("device_type", deviceType) - _deviceTypeCVRMean) / _deviceTypeCVRStd * _deviceTypeCVRCoef;
                op2 += (dataPackage.getMetrics("country", country) - _countryCVRMean) / _countryCVRStd * _countryCVRCoef;
                op2 += (dataPackage.getMetrics("city", city) - _cityCVRMean) / _cityCVRStd * _cityCVRCoef;
                op2 += (dataPackage.getMetrics("language", language) - _languageCVRMean) / _languageCVRStd * _languageCVRCoef;
                
                op2 += (dataPackage.getMetrics("hour", hour) - _hourCVRMean) / _hourCVRStd * _hourCVRCoef;
                op2 += (dataPackage.getMetrics("dow", dow) - _dowCVRMean) / _dowCVRStd * _dowCVRCoef;
                op2 += (dataPackage.getMetrics("weekday", weekday?"1":"0") - _weekdayCVRMean) / _weekdayCVRStd * _weekdayCVRCoef;
                
                summary = dataPackage.getSummary("context_metrics^exchange_rate_log_cvr");
                op2 += (dataPackage.getMetrics("exchange_rate_log", String.format("%.1f", exchangeRateLog)) - summary.getValue0()) / summary.getValue1() * dataPackage.getCoefficient("context_metrics^exchange_rate_log_cvr");
                

                double offerComp = dataPackage.getOfferPrediction(offerId);
                double appComp = dataPackage.getAppPrediction(appId);
                double userComp = 0.0;
                
                // summary = dataPackage.getSummary("context_metrics^offerwall_rank_cvr");

                double prob = 0.0;
                for (int rank = offerwallRankStart; rank <= offerwallRankEnd; ++rank) {
                        poly = _intercept + op1 + dataPackage.getCoefficient("context^offerwall_rank=" + ((Integer)rank).toString())
                                        + op2 + (dataPackage.getMetrics("offerwall_rank", ((Integer)rank).toString()) - _offerwallRankCVRMean) / _offerwallRankCVRStd * _offerwallRankCVRCoef
                                        + offerComp + appComp;
                        prob += _rankWeights.get(rank - 1) * (1 / (1 + Math.exp(- poly)));
                }

                return prob;
        }
        
        public double predict(
                        String source, 
                        String platform, 
                        String deviceType, 
                        String country,
                        String city,
                        String language,
                        int offerwallRankStart,
                        int offerwallRankEnd,
                        double exchangeRate,
                        String offerId,
                        String appId,
                        String userId)
        {
                Calendar cal = Calendar.getInstance(_tz);
                
                boolean weekday;
                String dow;
                
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.SUNDAY:
                        weekday = false;
                        dow = "Sun";
                        break;
                case Calendar.MONDAY:
                        weekday = true;
                        dow = "Mon";
                        break;
                case Calendar.TUESDAY:
                        weekday = true;
                        dow = "Tue";
                        break;
                case Calendar.WEDNESDAY:
                        weekday = true;
                        dow = "Wed";
                        break;
                case Calendar.THURSDAY:
                        weekday = true;
                        dow = "Thu";
                        break;
                case Calendar.FRIDAY:
                        weekday = true;
                        dow = "Fri";
                        break;
                case Calendar.SATURDAY:
                default:
                        weekday = false;
                        dow = "Sat";
                        break;
                }
                
                Integer h = cal.get(Calendar.HOUR_OF_DAY);
                String hour = h.toString();
                
                boolean daytime = (h >= 7 && h < 17);
                String ampm = (cal.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";

                return predict(source, platform, deviceType, country, city, language, offerwallRankStart, offerwallRankEnd,
                                weekday, dow, hour, daytime, ampm, exchangeRate, offerId, appId, userId);
        }
}