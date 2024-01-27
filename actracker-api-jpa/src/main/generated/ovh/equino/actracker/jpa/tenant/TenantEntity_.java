package ovh.equino.actracker.jpa.tenant;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(TenantEntity.class)
public abstract class TenantEntity_ extends ovh.equino.actracker.jpa.JpaEntity_ {

	
	/**
	 * @see ovh.equino.actracker.jpa.tenant.TenantEntity#password
	 **/
	public static volatile SingularAttribute<TenantEntity, String> password;
	
	/**
	 * @see ovh.equino.actracker.jpa.tenant.TenantEntity
	 **/
	public static volatile EntityType<TenantEntity> class_;
	
	/**
	 * @see ovh.equino.actracker.jpa.tenant.TenantEntity#username
	 **/
	public static volatile SingularAttribute<TenantEntity, String> username;

	public static final String PASSWORD = "password";
	public static final String USERNAME = "username";

}

