package com.slensky.focussis.data.network;

public class ApiProvider {

    private FocusNetApi netApi;
    private FocusDebugApi debugApi;
    private boolean useDebugApi;

    public ApiProvider(FocusNetApi netApi, FocusDebugApi debugApi) {
        this.netApi = netApi;
        this.debugApi = debugApi;
    }

    public boolean isUseDebugApi() {
        return useDebugApi;
    }

    public void setUseDebugApi(boolean useDebugApi) {
        this.useDebugApi = useDebugApi;
    }

    public FocusApi getApi() {
        return useDebugApi ? debugApi : netApi;
    }

}
