-- 카테고리 기본 데이터 삽입 (Spring Boot 초기화 스크립트용)
-- INSERT IGNORE를 사용하여 이미 데이터가 존재할 경우 중복 에러를 방지합니다.

INSERT IGNORE INTO category (category_id, name) VALUES (1, '친구 찾기');
INSERT IGNORE INTO category (category_id, name) VALUES (2, '질문');
INSERT IGNORE INTO category (category_id, name) VALUES (3, '소모임');
INSERT IGNORE INTO category (category_id, name) VALUES (4, '일상');

-- 3. 변경 사항을 DB에 영구적으로 반영
COMMIT;
