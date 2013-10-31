package com.tapjoy.opt.vertica_score.offerGenerator;


import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.tapjoy.opt.common.ColumnDef;
import com.tapjoy.opt.common.Row;

public class PaymentScoreTransformer implements ColumnDef {
    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DiscountScoreTransformer.class);

    public PaymentScoreTransformer(String os, int platform) {
    }

    public void transform(List<Row> offerlist){
        @SuppressWarnings("unused")
		Iterator<Row> it = offerlist.iterator();
        for (Row row : offerlist){
            @SuppressWarnings("unused")
			String offerId = row.getColumn(ID);


            double origScore = Double.parseDouble(row.getColumn(CONVERT_SCORE));
            double payment = Double.parseDouble(row.getColumn(PAYMENT));
            double bidOrig = Double.parseDouble(row.getColumn(BID));
            double newScore = 0;
            if(bidOrig != 0){
                newScore = origScore * payment / bidOrig;
            }

            row.setColumn(CONVERT_SCORE, "" + newScore);

        }

    }

}
