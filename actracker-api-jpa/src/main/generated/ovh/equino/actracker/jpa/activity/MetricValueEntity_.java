package ovh.equino.actracker.jpa.activity;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import java.math.BigDecimal;
import ovh.equino.actracker.jpa.tag.MetricEntity;

@StaticMetamodel(MetricValueEntity.class)
public abstract class MetricValueEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.activity.MetricValueEntity#activity
	 **/
	public static volatile SingularAttribute<MetricValueEntity, ActivityEntity> activity;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.MetricValueEntity#metric
	 **/
	public static volatile SingularAttribute<MetricValueEntity, MetricEntity> metric;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.MetricValueEntity
	 **/
	public static volatile EntityType<MetricValueEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.activity.MetricValueEntity#value
	 **/
	public static volatile SingularAttribute<MetricValueEntity, BigDecimal> value;

	public static final String ACTIVITY = "activity";
	public static final String METRIC = "metric";
	public static final String VALUE = "value";

}

