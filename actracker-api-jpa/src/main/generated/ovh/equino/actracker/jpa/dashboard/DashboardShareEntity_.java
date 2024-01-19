package ovh.equino.actracker.jpa.dashboard;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(DashboardShareEntity.class)
public abstract class DashboardShareEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardShareEntity#granteeId
	 **/
	public static volatile SingularAttribute<DashboardShareEntity, String> granteeId;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardShareEntity
	 **/
	public static volatile EntityType<DashboardShareEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardShareEntity#dashboard
	 **/
	public static volatile SingularAttribute<DashboardShareEntity, DashboardEntity> dashboard;
	
	/**
	 * @see ovh.equino.actracker.jpa.dashboard.DashboardShareEntity#granteeName
	 **/
	public static volatile SingularAttribute<DashboardShareEntity, String> granteeName;

	public static final String GRANTEE_ID = "granteeId";
	public static final String DASHBOARD = "dashboard";
	public static final String GRANTEE_NAME = "granteeName";

}

