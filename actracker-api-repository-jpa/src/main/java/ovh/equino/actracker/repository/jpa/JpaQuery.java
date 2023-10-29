package ovh.equino.actracker.repository.jpa;

public interface JpaQuery<T, R> {

    JpaQuery<T, R> where(JpaPredicate... conditions);

    R execute();
}
