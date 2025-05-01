package main.java.com.yutgame.model;

/**
 * 윷 던지기 결과 Enum.
 * - 빽도(BAK_DO) = -1칸
 * - 도(DO) = +1칸
 * - 개(GAE) = +2칸
 * - 걸(GEOL) = +3칸
 * - 윷(YUT) = +4칸 (추가 턴)
 * - 모(MO) = +5칸 (추가 턴)
 */
public enum YutThrowResult {
    BAK_DO,  // -1
    DO,      // +1
    GAE,     // +2
    GEOL,    // +3
    YUT,     // +4 (추가 턴)
    MO       // +5 (추가 턴)
}
