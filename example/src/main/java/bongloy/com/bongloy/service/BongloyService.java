package bongloy.com.bongloy.service;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface BongloyService {

    @POST("charge")
    Observable<Void> createQueryCharge(
            @Body Map<String, Object> params);
}
