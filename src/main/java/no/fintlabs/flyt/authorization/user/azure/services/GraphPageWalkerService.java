package no.fintlabs.flyt.authorization.user.azure.services;

import com.microsoft.graph.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@Slf4j
public class GraphPageWalkerService {

    public <
            T,
            R,
            REQUEST_BUILDER extends BaseRequestBuilder<T>,
            COLLECTION_RESPONSE extends BaseCollectionResponse<T>,
            COLLECTION_REQUEST_BUILDER extends BaseCollectionRequestBuilder<
                    T,
                    REQUEST_BUILDER,
                    COLLECTION_RESPONSE,
                    COLLECTION_PAGE,
                    COLLECTION_REQUEST
                    >,
            COLLECTION_PAGE extends BaseCollectionPage<T, COLLECTION_REQUEST_BUILDER>,
            COLLECTION_REQUEST extends BaseEntityCollectionRequest<T, COLLECTION_RESPONSE, COLLECTION_PAGE>
            >
    List<R> getContentFromCurrentAndNextPages(
            COLLECTION_REQUEST collectionRequest,
            Function<List<T>, List<R>> contentProcessing
    ) {
        return getContentFromCurrentAndNextPages(collectionRequest, BaseEntityCollectionRequest::get, contentProcessing);
    }

    public <
            T,
            R,
            REQUEST_BUILDER extends BaseRequestBuilder<T>,
            COLLECTION_RESPONSE extends BaseCollectionResponse<T>,
            COLLECTION_REQUEST_BUILDER extends BaseCollectionRequestBuilder<
                    T,
                    REQUEST_BUILDER,
                    COLLECTION_RESPONSE,
                    COLLECTION_PAGE,
                    COLLECTION_REQUEST
                    >,
            COLLECTION_PAGE extends BaseCollectionPage<T, COLLECTION_REQUEST_BUILDER>,
            COLLECTION_REQUEST extends BaseActionCollectionRequest<T, COLLECTION_RESPONSE, COLLECTION_PAGE>
            >
    List<R> getContentFromCurrentAndNextPages(
            COLLECTION_REQUEST collectionRequest,
            Function<List<T>, List<R>> contentProcessing
    ) {
        return getContentFromCurrentAndNextPages(collectionRequest, BaseActionCollectionRequest::post, contentProcessing);
    }

    public <
            T,
            R,
            REQUEST_BUILDER extends BaseRequestBuilder<T>,
            COLLECTION_RESPONSE extends BaseCollectionResponse<T>,
            COLLECTION_REQUEST_BUILDER extends BaseCollectionRequestBuilder<
                    T,
                    REQUEST_BUILDER,
                    COLLECTION_RESPONSE,
                    COLLECTION_PAGE,
                    COLLECTION_REQUEST
                    >,
            COLLECTION_PAGE extends BaseCollectionPage<T, COLLECTION_REQUEST_BUILDER>,
            COLLECTION_REQUEST extends BaseCollectionRequest<T, COLLECTION_RESPONSE, COLLECTION_PAGE>
            >
    List<R> getContentFromCurrentAndNextPages(
            COLLECTION_REQUEST collectionRequest,
            Function<COLLECTION_REQUEST, COLLECTION_PAGE> performRequest,
            Function<List<T>, List<R>> contentProcessing
    ) {
        COLLECTION_PAGE collectionPage = performRequest.apply(collectionRequest);
        if (collectionPage == null) {
            throw new IllegalStateException("Page is null");
        }
        List<R> content = contentProcessing.apply(new ArrayList<>(collectionPage.getCurrentPage()));
        COLLECTION_REQUEST_BUILDER nextBuilder = collectionPage.getNextPage();
        if (nextBuilder != null) {
            content.addAll(getContentFromCurrentAndNextPages(
                    collectionPage.getNextPage().buildRequest(),
                    performRequest,
                    contentProcessing
            ));
        }
        return content;
    }

}
