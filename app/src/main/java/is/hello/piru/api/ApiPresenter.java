package is.hello.piru.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import is.hello.piru.api.services.AdminService;
import is.hello.piru.api.services.CoreService;

@Singleton public final class ApiPresenter {
    @Inject AdminService adminService;
    @Inject CoreService coreService;
}
