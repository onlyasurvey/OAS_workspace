package com.oas.command.model;

import java.util.Map;

/**
 * Simple answer model for getting answers from the front end.
 * 
 * @author xhalliday
 * @since September 10, 2008
 */
public class SimpleAnswerCommand {

	// private long questionId;
	private String answer;
	private String otherText;
	private long[] choiceIdList;

	/** For summing questions, this maps a choiceId to the user's value, if any. */
	private Map<Long, Integer> sumByChoiceId;

	/**
	 * @return the answer
	 */
	public String getAnswer() {
		return answer;
	}

	/**
	 * CLFy setter
	 */
	public void setA(String answer) {
		setAnswer(answer);
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	/**
	 * @return the choiceIdList
	 */
	public long[] getChoiceIdList() {
		return choiceIdList;
	}

	/**
	 * @param choiceIdList
	 *            the choiceIdList to set
	 */
	public void setChoiceIdList(long[] choiceIdList) {
		this.choiceIdList = choiceIdList;
	}

	public void createChoiceIdListAsRange(long startId, long endId) {
		choiceIdList = new long[((int) endId - (int) startId) + 1];
		int count = 0;
		for (long i = startId; i <= endId; i++) {
			choiceIdList[count++] = i;
		}
	}

	/**
	 * @return the otherText
	 */
	public String getOtherText() {
		return otherText;
	}

	/**
	 * @param otherText
	 *            the otherText to set
	 */
	public void setOtherText(String otherText) {
		this.otherText = otherText;
	}

	/**
	 * @return the sumByChoiceId
	 */
	public Map<Long, Integer> getSumByChoiceId() {
		return sumByChoiceId;
	}

	/**
	 * @param sumByChoiceId
	 *            the sumByChoiceId to set
	 */
	public void setSumByChoiceId(Map<Long, Integer> sumByChoiceId) {
		this.sumByChoiceId = sumByChoiceId;
	}

}
