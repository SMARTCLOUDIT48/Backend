-- 1. 기존 데이터가 있는지 먼저 확인 (깔끔하게 비우고 시작하려면)
-- DELETE FROM category;

-- 2. 카테고리 데이터 삽입
-- (ID와 이름을 세트로 넣어서 자바 코드와 일치시킵니다)
--INSERT INTO category (category_id, name) VALUES (1, '친구 찾기');
--INSERT INTO category (category_id, name) VALUES (2, '질문');
--INSERT INTO category (category_id, name) VALUES (3, '소모임');
--INSERT INTO category (category_id, name) VALUES (4, '일상');
--
---- 3. 변경 사항을 DB에 영구적으로 반영
--COMMIT;

-- 4. 데이터가 잘 들어갔는지 최종 확인
SELECT * FROM category;