package ovh.equino.actracker.rest.spring;

public interface RestTestData<T> {

    String asHttpResponse();

    T asEntityResult();
}
