package com.tapjoy.opt.vertica_score.offerGenerator.transformer;

import java.util.Map;

import com.tapjoy.opt.common.Row;

public interface Transformer {

	public abstract Map<Row, Row> transform(Map<Row, Row> rowMap);

}