package com.be.recheckbe.global.init;

import com.be.recheckbe.domain.college.entity.College;
import com.be.recheckbe.domain.college.repository.CollegeRepository;
import com.be.recheckbe.domain.department.entity.Department;
import com.be.recheckbe.domain.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CollegeRepository collegeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (collegeRepository.count() > 0) return;

        Map<String, List<String>> data = Map.ofEntries(
                Map.entry("공과대학", List.of(
                        "기계공학과", "산업공학과", "화학공학과", "첨단신소재공학과",
                        "응용화학과", "환경안전공학과", "건설시스템공학과", "교통시스템공학과", "건축학과"
                )),
                Map.entry("첨단ICT융합대학", List.of(
                        "전자공학과", "지능형반도체공학과", "미래모빌리티공학과"
                )),
                Map.entry("소프트웨어융합대학", List.of(
                        "소프트웨어학과", "사이버보안학과", "디지털미디어학과", "국방디지털융합학과"
                )),
                Map.entry("자연과학대학", List.of(
                        "수학과", "프런티어과학학부"
                )),
                Map.entry("경영대학", List.of(
                        "경영학과", "경영인텔리전스학과", "금융공학과"
                )),
                Map.entry("인문대학", List.of(
                        "국어국문학과", "영어영문학과", "불어불문학과", "사학과", "문화콘텐츠학과"
                )),
                Map.entry("사회과학대학", List.of(
                        "행정학과", "심리학과", "스포츠레저학과", "경제정치사회융합학부"
                )),
                Map.entry("의과대학", List.of("의학과")),
                Map.entry("간호대학", List.of("간호학과")),
                Map.entry("약학대학", List.of("약학과")),
                Map.entry("첨단바이오융합대학", List.of("첨단바이오융합대학")),
                Map.entry("다산학부대학", List.of("자유전공학부(자연)", "자유전공학부(인문)"))
        );

        // 단과대 이름 순서 보장을 위해 순서 정의
        List<String> collegeOrder = List.of(
                "공과대학", "첨단ICT융합대학", "소프트웨어융합대학", "자연과학대학",
                "경영대학", "인문대학", "사회과학대학", "의과대학",
                "간호대학", "약학대학", "첨단바이오융합대학", "다산학부대학"
        );

        for (String collegeName : collegeOrder) {
            College college = collegeRepository.save(
                    College.builder().name(collegeName).build()
            );

            for (String deptName : data.get(collegeName)) {
                departmentRepository.save(
                        Department.builder()
                                .name(deptName)
                                .college(college)
                                .build()
                );
            }
        }
    }
}