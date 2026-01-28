package com.scit48.common.enums;

public enum InterestDetail {
	
	/* =========================
	 * 1. 문화 · 예술 (CULTURE)
	 * ========================= */
	MOVIE(InterestType.CULTURE, "영화"),
	DRAMA(InterestType.CULTURE, "드라마"),
	MUSIC(InterestType.CULTURE, "음악"),
	EXHIBITION(InterestType.CULTURE, "전시·미술관"),
	PERFORMANCE(InterestType.CULTURE, "공연·연극"),
	
	/* =========================
	 * 2. 취미 · 여가 (HOBBY)
	 * ========================= */
	PHOTO(InterestType.HOBBY, "사진"),
	GAME(InterestType.HOBBY, "게임"),
	BOARD_GAME(InterestType.HOBBY, "보드게임"),
	DIY(InterestType.HOBBY, "DIY·만들기"),
	COLLECT(InterestType.HOBBY, "수집"),
	
	/* =========================
	 * 3. 운동 · 스포츠 (SPORTS)
	 * ========================= */
	FITNESS(InterestType.SPORTS, "헬스·피트니스"),
	RUNNING(InterestType.SPORTS, "러닝·조깅"),
	YOGA(InterestType.SPORTS, "요가·필라테스"),
	BALL_SPORTS(InterestType.SPORTS, "구기 스포츠"),
	HIKING(InterestType.SPORTS, "등산·하이킹"),
	
	/* =========================
	 * 4. 여행 · 지역 (TRAVEL)
	 * ========================= */
	DOMESTIC_TRAVEL(InterestType.TRAVEL, "국내 여행"),
	OVERSEAS_TRAVEL(InterestType.TRAVEL, "해외 여행"),
	BACKPACKING(InterestType.TRAVEL, "배낭여행"),
	FOOD_TRIP(InterestType.TRAVEL, "맛집 탐방"),
	LOCAL_TOUR(InterestType.TRAVEL, "지역 산책·로컬 투어"),
	
	/* =========================
	 * 5. 음식 · 요리 (FOOD)
	 * ========================= */
	COOKING(InterestType.FOOD, "요리"),
	BAKING(InterestType.FOOD, "베이킹"),
	CAFE(InterestType.FOOD, "카페 투어"),
	ALCOHOL(InterestType.FOOD, "술·와인"),
	GOURMET(InterestType.FOOD, "미식 탐방"),
	
	/* =========================
	 * 6. 학습 · 자기계발 (STUDY)
	 * ========================= */
	LANGUAGE_STUDY(InterestType.STUDY, "언어 학습"),
	CERTIFICATE(InterestType.STUDY, "자격증 준비"),
	READING(InterestType.STUDY, "독서"),
	STUDY_GROUP(InterestType.STUDY, "스터디 모임"),
	CAREER(InterestType.STUDY, "커리어 개발"),
	
	/* =========================
	 * 7. IT · 기술 (IT)
	 * ========================= */
	PROGRAMMING(InterestType.IT, "프로그래밍"),
	WEB_APP(InterestType.IT, "웹·앱 개발"),
	GAME_DEV(InterestType.IT, "게임 개발"),
	AI_DATA(InterestType.IT, "AI·데이터"),
	IT_TREND(InterestType.IT, "IT 트렌드"),
	
	/* =========================
	 * 8. 라이프스타일 (LIFESTYLE)
	 * ========================= */
	DAILY(InterestType.LIFESTYLE, "일상 공유"),
	PET(InterestType.LIFESTYLE, "반려동물"),
	FASHION(InterestType.LIFESTYLE, "패션"),
	INTERIOR(InterestType.LIFESTYLE, "인테리어"),
	WELLNESS(InterestType.LIFESTYLE, "건강·웰빙");
	
	/* ========================= */
	
	private final InterestType type; // 대분류
	private final String value;      // 화면 표시용
	
	InterestDetail(InterestType type, String value) {
		this.type = type;
		this.value = value;
	}
	
	public InterestType getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
}
