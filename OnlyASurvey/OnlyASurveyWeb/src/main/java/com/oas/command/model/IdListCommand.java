package com.oas.command.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

/**
 * Command used when submitting a list of IDs
 * 
 * @author xhalliday
 * @since September 15, 2008
 */
public class IdListCommand {

	private List<Long> ids = new ArrayList<Long>();

	public IdListCommand() {
	}

	public IdListCommand(Long[] idList) {
		CollectionUtils.mergeArrayIntoCollection(idList, ids);
	}

	/**
	 * @return the ids
	 */
	public List<Long> getIds() {
		return ids;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public void addId(Long id) {
		ids.add(id);
	}

	/**
	 * Add a sequential list of ids.
	 * 
	 * @param startId
	 * @param endId
	 */
	public void addIdRange(Long startId, Long endId) {
		for (Long i = startId; i < endId; i++) {
			addId(i);
		}
	}
}
