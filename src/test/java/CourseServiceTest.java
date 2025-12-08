//package test;
//
//import main.java.dao.CourseDAO;
//import main.java.dao.EmployeeDAO;
//import main.java.entity.Course;
//import main.java.entity.Employee;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import main.java.service.CourseService;
//import main.java.service.CourseService.CourseDetail;
//import main.java.service.CourseService.CourseStatistics;
//import main.java.service.CourseService.ServiceResult;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
///**
// * CourseService 测试类
// *
// * 测试前提：
// * - 数据库中存在课程数据
// * - 存在教练（员工角色为Trainer）
// */
//public class CourseServiceTest {
//
//    private CourseService courseService;
//    private CourseDAO courseDAO;
//    private EmployeeDAO employeeDAO;
//    private int testCourseId;  // 用于清理测试数据
//    private int testTrainerId; // 测试用教练ID
//
//    @Before
//    public void setUp() {
//        courseService = new CourseService();
//        courseDAO = new CourseDAO();
//        employeeDAO = new EmployeeDAO();
//        testCourseId = 0;
//
//        // 获取一个教练ID用于测试
//        List<Employee> trainers = employeeDAO.getTrainers();
//        if (!trainers.isEmpty()) {
//            testTrainerId = trainers.get(0).getId();
//        } else {
//            testTrainerId = 1; // 默认使用ID=1
//        }
//    }
//
//    @After
//    public void tearDown() {
//        // 清理测试数据
//        if (testCourseId > 0) {
//            courseDAO.deleteCourse(testCourseId);
//            testCourseId = 0;
//        }
//    }
//
//    // ==================== 课程创建测试 ====================
//
//    @Test
//    public void testCreateCourse() {
//        ServiceResult<Course> result = courseService.createCourse(
//                "测试瑜伽课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//
//        assertTrue(result.isSuccess());
//        assertNotNull(result.getData());
//        assertEquals("测试瑜伽课程", result.getData().getName());
//        assertEquals(CourseService.TYPE_YOGA, result.getData().getType());
//        assertEquals(60, result.getData().getDuration());
//        assertEquals(20, result.getData().getMaxCapacity());
//
//        testCourseId = result.getData().getCourseId();
//    }
//
//    @Test
//    public void testCreateCourseWithEmptyName() {
//        ServiceResult<Course> result = courseService.createCourse(
//                "",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("名称不能为空"));
//    }
//
//    @Test
//    public void testCreateCourseWithInvalidType() {
//        ServiceResult<Course> result = courseService.createCourse(
//                "测试课程",
//                "invalid_type",
//                60,
//                20,
//                testTrainerId
//        );
//
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("无效的课程类型"));
//    }
//
//    @Test
//    public void testCreateCourseWithInvalidDuration() {
//        ServiceResult<Course> result = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                0,
//                20,
//                testTrainerId
//        );
//
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("时长必须大于0"));
//    }
//
//    @Test
//    public void testCreateCourseWithInvalidCapacity() {
//        ServiceResult<Course> result = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                0,
//                testTrainerId
//        );
//
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("容量必须大于0"));
//    }
//
//    @Test
//    public void testCreateCourseWithInvalidTrainer() {
//        ServiceResult<Course> result = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                99999
//        );
//
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("教练不存在"));
//    }
//
//    @Test
//    public void testCreateYogaCourse() {
//        ServiceResult<Course> result = courseService.createYogaCourse(
//                "测试瑜伽",
//                45,
//                15,
//                testTrainerId
//        );
//
//        assertTrue(result.isSuccess());
//        assertEquals(CourseService.TYPE_YOGA, result.getData().getType());
//
//        testCourseId = result.getData().getCourseId();
//    }
//
//    @Test
//    public void testCreateSpinningCourse() {
//        ServiceResult<Course> result = courseService.createSpinningCourse(
//                "测试动感单车",
//                45,
//                30,
//                testTrainerId
//        );
//
//        assertTrue(result.isSuccess());
//        assertEquals(CourseService.TYPE_SPINNING, result.getData().getType());
//
//        testCourseId = result.getData().getCourseId();
//    }
//
//    @Test
//    public void testCreatePilatesCourse() {
//        ServiceResult<Course> result = courseService.createPilatesCourse(
//                "测试普拉提",
//                50,
//                12,
//                testTrainerId
//        );
//
//        assertTrue(result.isSuccess());
//        assertEquals(CourseService.TYPE_PILATES, result.getData().getType());
//
//        testCourseId = result.getData().getCourseId();
//    }
//
//    @Test
//    public void testCreateAerobicsCourse() {
//        ServiceResult<Course> result = courseService.createAerobicsCourse(
//                "测试有氧操",
//                60,
//                25,
//                testTrainerId
//        );
//
//        assertTrue(result.isSuccess());
//        assertEquals(CourseService.TYPE_AEROBICS, result.getData().getType());
//
//        testCourseId = result.getData().getCourseId();
//    }
//
//    @Test
//    public void testCreateStrengthCourse() {
//        ServiceResult<Course> result = courseService.createStrengthCourse(
//                "测试力量训练",
//                90,
//                10,
//                testTrainerId
//        );
//
//        assertTrue(result.isSuccess());
//        assertEquals(CourseService.TYPE_STRENGTH, result.getData().getType());
//
//        testCourseId = result.getData().getCourseId();
//    }
//
//    // ==================== 课程信息管理测试 ====================
//
//    @Test
//    public void testUpdateCourseInfo() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "待更新课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        // 更新信息
//        ServiceResult<Course> updateResult = courseService.updateCourseInfo(
//                testCourseId,
//                "已更新课程",
//                CourseService.TYPE_SPINNING,
//                90,
//                30
//        );
//
//        assertTrue(updateResult.isSuccess());
//        assertEquals("已更新课程", updateResult.getData().getName());
//        assertEquals(CourseService.TYPE_SPINNING, updateResult.getData().getType());
//        assertEquals(90, updateResult.getData().getDuration());
//        assertEquals(30, updateResult.getData().getMaxCapacity());
//    }
//
//    @Test
//    public void testUpdateCourseInfoNotFound() {
//        ServiceResult<Course> result = courseService.updateCourseInfo(
//                99999,
//                "测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20
//        );
//
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("课程不存在"));
//    }
//
//    @Test
//    public void testUpdateCourseName() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "原名称",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        // 更新名称
//        ServiceResult<Course> updateResult = courseService.updateCourseName(testCourseId, "新名称");
//
//        assertTrue(updateResult.isSuccess());
//        assertEquals("新名称", updateResult.getData().getName());
//    }
//
//    @Test
//    public void testUpdateCourseNameEmpty() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        ServiceResult<Course> result = courseService.updateCourseName(testCourseId, "");
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("名称不能为空"));
//    }
//
//    @Test
//    public void testUpdateCourseDuration() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        // 更新时长
//        ServiceResult<Course> updateResult = courseService.updateCourseDuration(testCourseId, 90);
//
//        assertTrue(updateResult.isSuccess());
//        assertEquals(90, updateResult.getData().getDuration());
//    }
//
//    @Test
//    public void testUpdateCourseDurationInvalid() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        ServiceResult<Course> result = courseService.updateCourseDuration(testCourseId, 0);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("时长必须大于0"));
//    }
//
//    @Test
//    public void testUpdateCourseCapacity() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        // 更新容量
//        ServiceResult<Course> updateResult = courseService.updateCourseCapacity(testCourseId, 30);
//
//        assertTrue(updateResult.isSuccess());
//        assertEquals(30, updateResult.getData().getMaxCapacity());
//    }
//
//    @Test
//    public void testUpdateCourseCapacityInvalid() {
//        // 先创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        ServiceResult<Course> result = courseService.updateCourseCapacity(testCourseId, 0);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("容量必须大于0"));
//    }
//
//    // ==================== 教练分配测试 ====================
//
//    @Test
//    public void testChangeTrainer() {
//        // 获取另一个教练
//        List<Employee> trainers = employeeDAO.getTrainers();
//        if (trainers.size() < 2) {
//            System.out.println("跳过测试：需要至少2个教练");
//            return;
//        }
//
//        int trainer1Id = trainers.get(0).getId();
//        int trainer2Id = trainers.get(1).getId();
//
//        // 创建课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "教练更换测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                trainer1Id
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        // 更换教练
//        ServiceResult<Course> changeResult = courseService.changeTrainer(testCourseId, trainer2Id);
//
//        assertTrue(changeResult.isSuccess());
//        assertEquals(trainer2Id, changeResult.getData().getEmployeeId());
//    }
//
//    @Test
//    public void testChangeTrainerNotFound() {
//        ServiceResult<Course> result = courseService.changeTrainer(99999, testTrainerId);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("课程不存在"));
//    }
//
//    @Test
//    public void testChangeTrainerInvalidTrainer() {
//        // 创建课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        ServiceResult<Course> result = courseService.changeTrainer(testCourseId, 99999);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("教练不存在"));
//    }
//
//    @Test
//    public void testChangeTrainerSameTrainer() {
//        // 创建课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        ServiceResult<Course> result = courseService.changeTrainer(testCourseId, testTrainerId);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("已是本课程的教练"));
//    }
//
//    // ==================== 课程删除测试 ====================
//
//    @Test
//    public void testDeleteCourse() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "待删除课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        int courseId = createResult.getData().getCourseId();
//
//        // 删除
//        ServiceResult<Void> deleteResult = courseService.deleteCourse(courseId);
//        assertTrue(deleteResult.isSuccess());
//
//        // 验证已删除
//        assertNull(courseDAO.getCourseById(courseId));
//
//        testCourseId = 0;  // 已删除，不需要清理
//    }
//
//    @Test
//    public void testDeleteCourseNotFound() {
//        ServiceResult<Void> result = courseService.deleteCourse(99999);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("课程不存在"));
//    }
//
//    // ==================== 课程查询测试 ====================
//
//    @Test
//    public void testGetCourseById() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "ID查询测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        Course course = courseService.getCourseById(testCourseId);
//        assertNotNull(course);
//        assertEquals(testCourseId, course.getCourseId());
//    }
//
//    @Test
//    public void testGetCourseByIdNotFound() {
//        Course course = courseService.getCourseById(99999);
//        assertNull(course);
//    }
//
//    @Test
//    public void testGetAllCourses() {
//        List<Course> courses = courseService.getAllCourses();
//        assertNotNull(courses);
//    }
//
//    @Test
//    public void testSearchByName() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "搜索测试课程ABC",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        List<Course> courses = courseService.searchByName("搜索测试");
//        assertNotNull(courses);
//        assertTrue(courses.size() > 0);
//        for (Course course : courses) {
//            assertTrue(course.getName().contains("搜索测试"));
//        }
//    }
//
//    @Test
//    public void testGetCoursesByType() {
//        List<Course> yogaCourses = courseService.getCoursesByType(CourseService.TYPE_YOGA);
//        assertNotNull(yogaCourses);
//        for (Course course : yogaCourses) {
//            assertEquals(CourseService.TYPE_YOGA, course.getType());
//        }
//    }
//
//    @Test
//    public void testGetCoursesByTrainer() {
//        List<Course> courses = courseService.getCoursesByTrainer(testTrainerId);
//        assertNotNull(courses);
//        for (Course course : courses) {
//            assertEquals(testTrainerId, course.getEmployeeId());
//        }
//    }
//
//    @Test
//    public void testGetCoursesByDurationRange() {
//        List<Course> courses = courseService.getCoursesByDurationRange(30, 90);
//        assertNotNull(courses);
//        for (Course course : courses) {
//            assertTrue(course.getDuration() >= 30 && course.getDuration() <= 90);
//        }
//    }
//
//    @Test
//    public void testGetAvailableCourses() {
//        List<Course> courses = courseService.getAvailableCourses();
//        assertNotNull(courses);
//        for (Course course : courses) {
//            assertFalse(courseService.isFull(course.getCourseId()));
//        }
//    }
//
//    @Test
//    public void testSearch() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "综合搜索测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        // 按名称搜索
//        List<Course> nameResults = courseService.search("综合搜索");
//        assertNotNull(nameResults);
//        assertTrue(nameResults.size() > 0);
//
//        // 按类型搜索
//        List<Course> typeResults = courseService.search("yoga");
//        assertNotNull(typeResults);
//
//        // 空搜索返回所有
//        List<Course> allResults = courseService.search("");
//        assertNotNull(allResults);
//        assertEquals(courseService.getAllCourses().size(), allResults.size());
//    }
//
//    // ==================== 课程详情测试 ====================
//
//    @Test
//    public void testGetCourseDetail() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "详情测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        CourseDetail detail = courseService.getCourseDetail(testCourseId);
//        assertNotNull(detail);
//        assertNotNull(detail.getCourse());
//        assertEquals(testCourseId, detail.getCourse().getCourseId());
//        assertNotNull(detail.getTypeDisplayName());
//        assertNotNull(detail.getDurationFormatted());
//        assertNotNull(detail.getTrainerName());
//        assertTrue(detail.getAvailableSlots() >= 0);
//
//        // 打印详情
//        System.out.println(detail.toString());
//    }
//
//    @Test
//    public void testGetCourseDetailNotFound() {
//        CourseDetail detail = courseService.getCourseDetail(99999);
//        assertNull(detail);
//    }
//
//    // ==================== 课程容量管理测试 ====================
//
//    @Test
//    public void testGetAvailableSlots() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "容量测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        int slots = courseService.getAvailableSlots(testCourseId);
//        assertEquals(20, slots);  // 新课程应该全部可用
//    }
//
//    @Test
//    public void testIsFull() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "满员测试课程",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        boolean full = courseService.isFull(testCourseId);
//        assertFalse(full);  // 新课程不应该满
//    }
//
//    @Test
//    public void testGetConfirmedBookingCount() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "预约统计测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        int count = courseService.getConfirmedBookingCount(testCourseId);
//        assertEquals(0, count);  // 新课程应该没有预约
//    }
//
//    @Test
//    public void testValidateCourseAvailable() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "可用性验证测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        ServiceResult<Course> result = courseService.validateCourseAvailable(testCourseId);
//        assertTrue(result.isSuccess());
//    }
//
//    @Test
//    public void testValidateCourseAvailableNotFound() {
//        ServiceResult<Course> result = courseService.validateCourseAvailable(99999);
//        assertFalse(result.isSuccess());
//        assertTrue(result.getMessage().contains("不存在"));
//    }
//
//    // ==================== 课程统计测试 ====================
//
//    @Test
//    public void testGetTotalCourseCount() {
//        int count = courseService.getTotalCourseCount();
//        assertTrue(count >= 0);
//    }
//
//    @Test
//    public void testGetCourseCountByType() {
//        Map<String, Integer> countMap = courseService.getCourseCountByType();
//        assertNotNull(countMap);
//        assertTrue(countMap.containsKey(CourseService.TYPE_YOGA));
//        assertTrue(countMap.containsKey(CourseService.TYPE_SPINNING));
//        assertTrue(countMap.containsKey(CourseService.TYPE_PILATES));
//        assertTrue(countMap.containsKey(CourseService.TYPE_AEROBICS));
//        assertTrue(countMap.containsKey(CourseService.TYPE_STRENGTH));
//        assertTrue(countMap.containsKey(CourseService.TYPE_OTHER));
//    }
//
//    @Test
//    public void testGetCourseCountByTrainer() {
//        Map<Integer, Integer> countMap = courseService.getCourseCountByTrainer();
//        assertNotNull(countMap);
//    }
//
//    @Test
//    public void testGetStatistics() {
//        CourseStatistics stats = courseService.getStatistics();
//        assertNotNull(stats);
//        assertTrue(stats.getTotalCount() >= 0);
//        assertTrue(stats.getAvailableRate() >= 0 && stats.getAvailableRate() <= 100);
//
//        // 打印统计
//        System.out.println(stats.toString());
//    }
//
//    // ==================== 工具方法测试 ====================
//
//    @Test
//    public void testGetAllCourseTypes() {
//        List<String> types = courseService.getAllCourseTypes();
//        assertNotNull(types);
//        assertEquals(6, types.size());
//        assertTrue(types.contains(CourseService.TYPE_YOGA));
//        assertTrue(types.contains(CourseService.TYPE_SPINNING));
//        assertTrue(types.contains(CourseService.TYPE_PILATES));
//        assertTrue(types.contains(CourseService.TYPE_AEROBICS));
//        assertTrue(types.contains(CourseService.TYPE_STRENGTH));
//        assertTrue(types.contains(CourseService.TYPE_OTHER));
//    }
//
//    @Test
//    public void testIsValidType() {
//        assertTrue(courseService.isValidType(CourseService.TYPE_YOGA));
//        assertTrue(courseService.isValidType(CourseService.TYPE_SPINNING));
//        assertTrue(courseService.isValidType(CourseService.TYPE_PILATES));
//        assertTrue(courseService.isValidType(CourseService.TYPE_AEROBICS));
//        assertTrue(courseService.isValidType(CourseService.TYPE_STRENGTH));
//        assertTrue(courseService.isValidType(CourseService.TYPE_OTHER));
//        assertFalse(courseService.isValidType("invalid"));
//        assertFalse(courseService.isValidType(null));
//    }
//
//    @Test
//    public void testGetTypeDisplayName() {
//        assertEquals("瑜伽", courseService.getTypeDisplayName(CourseService.TYPE_YOGA));
//        assertEquals("动感单车", courseService.getTypeDisplayName(CourseService.TYPE_SPINNING));
//        assertEquals("普拉提", courseService.getTypeDisplayName(CourseService.TYPE_PILATES));
//        assertEquals("有氧操", courseService.getTypeDisplayName(CourseService.TYPE_AEROBICS));
//        assertEquals("力量训练", courseService.getTypeDisplayName(CourseService.TYPE_STRENGTH));
//        assertEquals("其他", courseService.getTypeDisplayName(CourseService.TYPE_OTHER));
//        assertEquals("未知", courseService.getTypeDisplayName("invalid"));
//    }
//
//    @Test
//    public void testFormatDuration() {
//        assertEquals("30分钟", courseService.formatDuration(30));
//        assertEquals("1小时", courseService.formatDuration(60));
//        assertEquals("1小时30分钟", courseService.formatDuration(90));
//        assertEquals("2小时", courseService.formatDuration(120));
//        assertEquals("0分钟", courseService.formatDuration(0));
//    }
//
//    @Test
//    public void testIsCourseExists() {
//        // 创建测试课程
//        ServiceResult<Course> createResult = courseService.createCourse(
//                "存在性测试",
//                CourseService.TYPE_YOGA,
//                60,
//                20,
//                testTrainerId
//        );
//        assertTrue(createResult.isSuccess());
//        testCourseId = createResult.getData().getCourseId();
//
//        assertTrue(courseService.isCourseExists(testCourseId));
//        assertFalse(courseService.isCourseExists(99999));
//    }
//
//    // ==================== ServiceResult 测试 ====================
//
//    @Test
//    public void testServiceResultSuccess() {
//        ServiceResult<String> result = ServiceResult.success("操作成功", "数据");
//        assertTrue(result.isSuccess());
//        assertEquals("操作成功", result.getMessage());
//        assertEquals("数据", result.getData());
//    }
//
//    @Test
//    public void testServiceResultSuccessNoData() {
//        ServiceResult<String> result = ServiceResult.success("操作成功");
//        assertTrue(result.isSuccess());
//        assertEquals("操作成功", result.getMessage());
//        assertNull(result.getData());
//    }
//
//    @Test
//    public void testServiceResultFailure() {
//        ServiceResult<String> result = ServiceResult.failure("操作失败");
//        assertFalse(result.isSuccess());
//        assertEquals("操作失败", result.getMessage());
//        assertNull(result.getData());
//    }
//
//    @Test
//    public void testServiceResultToString() {
//        ServiceResult<String> success = ServiceResult.success("成功消息");
//        assertTrue(success.toString().contains("成功"));
//
//        ServiceResult<String> failure = ServiceResult.failure("失败消息");
//        assertTrue(failure.toString().contains("失败"));
//    }
//}
//
