package recommender.persistence.manager;

import org.junit.*;
import recommender.persistence.manager.dto.user.CreateDTO;
import recommender.persistence.manager.dto.user.GetDTO;
import recommender.persistence.manager.dto.user.UpdateDTO;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class UserManagerTests {
    private static UserManager userManager;

    @BeforeClass
    public static void setUp() {
        userManager = new UserManager();
    }

    @Before
    @After
    public void cleanUp() {
        userManager.listUsers()
                .forEach(
                        user -> userManager.deleteUser(user.getUid())
                );
    }

    @Test
    public void addUserTest() {
        CreateDTO createDTO = CreateDTO.builder()
                .username("gustavoaca")
                .build();
        Integer uid = userManager.addUser(createDTO);
        Assert.assertNotNull("User's id is null", uid);
    }

    @Test
    public void getUserTest() {
        CreateDTO createDTO = CreateDTO.builder()
                .username("gustavoaca")
                .build();
        Integer uid = userManager.addUser(createDTO);
        GetDTO getDTO = userManager.getUser(uid);
        Assert.assertNotNull("DTO is null", getDTO);
        Assert.assertEquals("Usernames don't match",
                "gustavoaca", getDTO.getUsername());
    }

    @Test(expected = NoSuchElementException.class)
    public void getUserNotFoundTest() {
        userManager.getUser(0);
    }

    @Test
    public void listUsersTest() {
        List<CreateDTO> createDTOS = Arrays.asList(
                CreateDTO.builder()
                        .username("gustavoaca")
                        .build(),
                CreateDTO.builder()
                        .username("lisalfonzo")
                        .build(),
                CreateDTO.builder()
                        .username("gcastellanos")
                        .build()
        );
        createDTOS.forEach(
                user -> userManager.addUser(user));

        List<GetDTO> getDTOS = userManager.listUsers();
        Assert.assertNotNull("List of users is null", getDTOS);

        List<String> usernames = getDTOS.stream()
                .map(GetDTO::getUsername).collect(Collectors.toList());

        createDTOS.forEach(
                user ->
                        Assert.assertTrue(
                                String.format("Username %s is not in the list",
                                        user.getUsername()),
                                usernames.contains(user.getUsername())));
    }

    @Test(expected = NoSuchElementException.class)
    public void deleteUserTest() {
        CreateDTO createDTO = CreateDTO.builder()
                .username("gustavoaca")
                .build();
        Integer uid = userManager.addUser(createDTO);
        userManager.deleteUser(uid);
        try {
            userManager.getUser(uid);
        } catch (NoSuchElementException e) {
            Assert.assertEquals("Error messages don't match",
                    String.format("User with id %s not found.", uid),
                    e.getMessage());
            throw e;
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void deleteUserNotFoundTest() {
        userManager.deleteUser(0);
    }

    @Test
    public void updateUserTest() {
        CreateDTO createDTO = CreateDTO.builder()
                .username("gustavoaca")
                .build();
        Integer uid = userManager.addUser(createDTO);
        UpdateDTO updateDTO = UpdateDTO.updateBuilder()
                .username("gcastellanos")
                .uid(uid)
                .build();
        userManager.updateUser(updateDTO);

        GetDTO getDTO = userManager.getUser(uid);
        Assert.assertNotNull("User is null", getDTO);
        Assert.assertEquals("Usernames don't match",
                "gcastellanos",
                getDTO.getUsername());
    }

    @Test(expected = NoSuchElementException.class)
    public void updateUserNotFoundTest() {
        UpdateDTO updateDTO = UpdateDTO.updateBuilder()
                .username("gcastellanos")
                .uid(0)
                .build();
        userManager.updateUser(updateDTO);
    }
}
