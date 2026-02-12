package com.scit48.recommend.criteria;

import com.scit48.common.enums.InterestType;
import com.scit48.recommend.service.MatchService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Getter
@NoArgsConstructor
public class Criteria {
	private String gender = "ANY";
	private int ageMin = 18;
	private int ageMax = 80;
	private String nation = "ANY";
	private String studyLang = "ANY";
	
	private boolean levelsAny = true;
	private Set<Integer> levels = new HashSet<>();
	
	private boolean interestsAny = true;
	private Set<InterestType> interests = new HashSet<>();
	
	public static Criteria parse(String key) {
		Criteria c = new Criteria();
		if (key == null || key.isBlank()) return c;
		
		String[] parts = key.split("\\|");
		Map<String, String> map = new HashMap<>();
		for (String p : parts) {
			String[] kv = p.split("=", 2);
			if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
		}
		
		c.gender = map.getOrDefault("g", "ANY").toUpperCase();
		
		// age=20-30
		String age = map.get("age");
		if (age != null && age.contains("-")) {
			try {
				String[] rr = age.split("-", 2);
				c.ageMin = Integer.parseInt(rr[0].trim());
				c.ageMax = Integer.parseInt(rr[1].trim());
				if (c.ageMin > c.ageMax) {
					int tmp = c.ageMin;
					c.ageMin = c.ageMax;
					c.ageMax = tmp;
				}
			} catch (Exception ignored) { }
		}
		
		c.nation = map.getOrDefault("n", "ANY").toUpperCase();
		c.studyLang = map.getOrDefault("lang", "ANY").toUpperCase();
		
		// lv=ANY or lv=1,2,3
		String lv = map.getOrDefault("lv", "ANY").toUpperCase();
		if (!"ANY".equals(lv)) {
			c.levelsAny = false;
			for (String s : lv.split(",")) {
				try {
					int v = Integer.parseInt(s.trim());
					if (v >= 1 && v <= 4) c.levels.add(v);
				} catch (Exception ignored) { }
			}
			if (c.levels.isEmpty()) c.levelsAny = true;
		}
		
		// interest=ANY or interest=CULTURE,IT
		String it = map.getOrDefault("interest", "ANY").toUpperCase();
		if (!"ANY".equals(it)) {
			c.interestsAny = false;
			for (String s : it.split(",")) {
				String name = s.trim();
				if (name.isEmpty()) continue;
				try {
					c.interests.add(InterestType.valueOf(name));
				} catch (Exception ignored) {
					log.debug("Invalid interest token: {}", name);
				}
			}
			if (c.interests.isEmpty()) c.interestsAny = true;
		}
		
		return c;
	}
}
