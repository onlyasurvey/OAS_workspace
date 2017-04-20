package com.oas.model.question;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.oas.model.Question;
import com.oas.model.Survey;

/**
 * Boolean question type.
 * 
 * @author Jason Halliday
 * @since September 5, 2008
 */
@Entity
@Table(schema = "oas", name = "boolean_question")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "baseObjectSequence", sequenceName = "oas.base_object_id_seq")
@PrimaryKeyJoinColumns( { @PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id") })
public class BooleanQuestion extends Question {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5987397772710904149L;

	public BooleanQuestion() {
	}

	public BooleanQuestion(Survey survey) {
		super(survey);
	}

	public BooleanQuestion(Survey survey, Long displayOrder) {
		super(survey, displayOrder);
	}

}
