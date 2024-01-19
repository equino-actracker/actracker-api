package ovh.equino.actracker.jpa.dashboard;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;
import ovh.equino.actracker.jpa.tag.TagEntity;

@StaticMetamodel(ChartEntity.class)
public abstract class ChartEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity#deleted
	 **/
	public static volatile SingularAttribute<ChartEntity, Boolean> deleted;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity#metric
	 **/
	public static volatile SingularAttribute<ChartEntity, String> metric;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity#name
	 **/
	public static volatile SingularAttribute<ChartEntity, String> name;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity#groupBy
	 **/
	public static volatile SingularAttribute<ChartEntity, String> groupBy;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity
	 **/
	public static volatile EntityType<ChartEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity#dashboard
	 **/
	public static volatile SingularAttribute<ChartEntity, DashboardEntity> dashboard;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.ChartEntity#tags
	 **/
	public static volatile SetAttribute<ChartEntity, TagEntity> tags;

	public static final String DELETED = "deleted";
	public static final String METRIC = "metric";
	public static final String NAME = "name";
	public static final String GROUP_BY = "groupBy";
	public static final String DASHBOARD = "dashboard";
	public static final String TAGS = "tags";

}

