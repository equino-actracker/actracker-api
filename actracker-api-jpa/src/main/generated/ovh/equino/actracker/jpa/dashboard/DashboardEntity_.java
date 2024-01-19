package ovh.equino.actracker.jpa.dashboard;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DashboardEntity.class)
public abstract class DashboardEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardEntity#shares
	 **/
	public static volatile ListAttribute<DashboardEntity, DashboardShareEntity> shares;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardEntity#charts
	 **/
	public static volatile ListAttribute<DashboardEntity, ChartEntity> charts;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardEntity#deleted
	 **/
	public static volatile SingularAttribute<DashboardEntity, Boolean> deleted;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardEntity#creatorId
	 **/
	public static volatile SingularAttribute<DashboardEntity, String> creatorId;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardEntity#name
	 **/
	public static volatile SingularAttribute<DashboardEntity, String> name;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardEntity
	 **/
	public static volatile EntityType<DashboardEntity> class_;

	public static final String SHARES = "shares";
	public static final String CHARTS = "charts";
	public static final String DELETED = "deleted";
	public static final String CREATOR_ID = "creatorId";
	public static final String NAME = "name";

}

