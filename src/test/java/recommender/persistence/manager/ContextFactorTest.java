package recommender.persistence.manager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import recommender.persistence.entity.ContextFactor;
import recommender.persistence.entity.Relevance;
import recommender.persistence.entity.User;

import java.util.Arrays;
import java.util.HashSet;

// TODO
public class ContextFactorTest {
    private static ContextManager contextManager;
    private static UserManager userManager;
    private static User admin;

    @BeforeClass
    public static void setUp() {
        contextManager = new ContextManager();
        userManager = new UserManager();
        admin = User.builder()
                .username("admin")
                .classPropertiesSet(new HashSet<>())
                .build();
        admin.setUid(userManager.addUser(admin));

        Arrays.asList("weather", "time", "day")
                .forEach(cname -> contextManager.addContextFactor(
                        ContextFactor.builder()
                                .name(cname).build()));
    }

    @AfterClass
    public static void cleanUp() {
        contextManager.listContextFactors()
                .stream()
                .map(ContextFactor::getCid)
                .forEach(cid -> contextManager.deleteContextFactor(cid));
        userManager.deleteUser(admin.getUid());
    }

    @After
    public void cleanRelevances() {
        contextManager.listRelevancesByUserId(admin.getUid())
                .stream()
                .map(Relevance::getRid)
                .forEach(rid -> contextManager.deleteRelevance(rid));
    }

    @Test
    public void addContextFactor() {
        ContextFactor contextFactor = ContextFactor.builder()
                .name("temperature")
                .build();
        Integer cid = contextManager.addContextFactor(contextFactor);
        Assert.assertNotNull("Id is null", cid);
    }

    @Test(expected = IllegalStateException.class)
    public void addDuplicateContextFactor() throws IllegalStateException {
        ContextFactor contextFactor0 = ContextFactor.builder()
                .name("happiness")
                .build();
        contextManager.addContextFactor(contextFactor0);
        ContextFactor contextFactor1 = ContextFactor.builder()
                .name("happiness")
                .build();
        contextManager.addContextFactor(contextFactor1);
        Assert.fail();
    }

}
