<%@page import="com.oas.model.util.QuestionTypeCode;"%><%@ include
	file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="createQuestion.pageTitle" /></title>
<c:set var="defaultQuestionTitle"><fmt:message key='createQuestion.defaultNewName' /></c:set>

<p><a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>"><spring:message code='backLink.questionsTab' /></a></p>

<h1><fmt:message key="createQuestion.pageTitle" /></h1>
<p><fmt:message key="createQuestion.introText" /></p>


<%-- ---------------------------------------------

	GENERAL QUESTION TYPES

     ---------------------------------------------
--%>
<div class="yui-g">
	<h2><fmt:message key="createQuestion.type.general" /></h2>
</div>
<div class="yui-g seperator">
	<div class="yui-u first">
		<h3><fmt:message key="createQuestion.type.page" /></h3>		
		<c:set var="typeCode"><%=QuestionTypeCode.PAGE %></c:set>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input name="${typeCode}" type="submit" class="button"
				title="<fmt:message key="createQuestion.type.pageAlt" />"
				value="<fmt:message key='createQuestion.addButton'/>" />
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/question_page_section.gif'/>"
			alt="<fmt:message key="createQuestion.type.textFieldAlt" />" title="<fmt:message key="createQuestion.type.textFieldAlt" />"
			/>
	</div>
</div>


<%-- ---------------------------------------------

	TEXT QUESTION TYPES

     ---------------------------------------------
--%>
<div class="yui-g">
	<h2><fmt:message key="createQuestion.type.openTextFields" /></h2>
</div>
<div class="yui-g seperator">
	<div class="yui-u first">
		<h3><fmt:message key="createQuestion.type.textField" /></h3>		
		<c:set var="typeCode"><%=QuestionTypeCode.TEXT %></c:set>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input name="${typeCode}" type="submit" class="button"
				title="<fmt:message key="createQuestion.type.textFieldAlt" />"
				value="<fmt:message key='createQuestion.addButton'/>" />
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/text_field.png'/>"
			alt="<fmt:message key="createQuestion.type.textFieldAlt" />" title="<fmt:message key="createQuestion.type.textFieldAlt" />"
			/>
	</div>
</div>

<div class="yui-g ">
	<div class="yui-u first">
		<h3>
			<fmt:message key="createQuestion.type.essay" />
		</h3>
		<c:set var="typeCode"><%=QuestionTypeCode.ESSAY %></c:set>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input name="${typeCode}" type="submit"
				class="button" title="<fmt:message key="createQuestion.type.essayAlt" />"
				value="<fmt:message key='createQuestion.addButton'/>"/>
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/text_area_essay_question.png'/>"
			alt="<fmt:message key="createQuestion.type.essayQuestionAlt" />" title="<fmt:message key="createQuestion.type.essayAlt" />"
			/>
	</div>
</div>


<%-- --------------------------------------------- 

	Multiple-Choice
	
	 --------------------------------------------- 
--%>
<div class="yui-g">
	<h2>
		<fmt:message key='createQuestion.type.multipleChoice' />
	</h2>
</div>

<div class="yui-g seperator">
	<div class="yui-u first">
		<h3>
			<fmt:message key="createQuestion.type.radio" />
		</h3>
		<c:set var="typeCode"><%=QuestionTypeCode.RADIO %></c:set>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input name="${typeCode}" type="submit" class="button"
				title="<fmt:message key="createQuestion.type.radioAlt" />"
				value="<fmt:message key='createQuestion.addButton'/>"/>
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/radio_buttons.png'/>"
			alt="<fmt:message key="createQuestion.type.radioAlt" />" title="<fmt:message key="createQuestion.type.radioAlt" />"
			/>
	</div>
</div>

<div class="yui-g seperator">
	<div class="yui-u first">
		<h3>
			<fmt:message key="createQuestion.type.checkboxes" />
		</h3>
		<c:set var="typeCode"><%=QuestionTypeCode.CHECKBOX %></c:set>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input title="<fmt:message key="createQuestion.type.checkboxesAlt" />" name="${typeCode}" class="button"
				type="submit" value="<fmt:message key='createQuestion.addButton'/>"/>
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/check_boxes.png'/>"
			alt="<fmt:message key="createQuestion.type.checkboxesAlt" />" title="<fmt:message key="createQuestion.type.checkboxesAlt" />"
			/>
	</div>
</div>

<div class="yui-g seperator">
	<div class="yui-u first">
		<h3>
			<fmt:message key="createQuestion.type.selectList" />
		</h3>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input title="<fmt:message key="createQuestion.type.selectListAlt" />" name="<%=QuestionTypeCode.SELECT%>" class="button"
				type="submit" value="<fmt:message key='createQuestion.addButton'/>"/>
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/select_list.png'/>"
			alt="<fmt:message key="createQuestion.type.selectListAlt" />" title="<fmt:message key="createQuestion.type.selectListAlt" />"
			/>
	</div>
</div>

<div class="yui-g seperator">
	<div class="yui-u first">
		<h3>
			<fmt:message key="createQuestion.type.ratingScale" />
		</h3>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input title="<fmt:message key="createQuestion.type.ratingScaleAlt" />" name="<%=QuestionTypeCode.SCALE%>" class="button"
				type="submit" value="<fmt:message key='createQuestion.addButton'/>"/>
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/rating_scale.png'/>"
			alt="<fmt:message key="createQuestion.type.ratingScaleAlt" />" title="<fmt:message key="createQuestion.type.ratingScaleAlt" />"
			/>
	</div>
</div>

<div class="yui-g seperator">
	<div class="yui-u first">
		<h3>
			<fmt:message key="createQuestion.type.constantSum" />
		</h3>
		<form:form id="" action="${survey.id}.html" method="get"><div>
			<input title="<fmt:message key="createQuestion.type.constantSumAlt" />" name="<%=QuestionTypeCode.CONSTANT_SUM%>" class="button"
				type="submit" value="<fmt:message key='createQuestion.addButton'/>"/>
		</div></form:form>
	</div>
	<div class="yui-u">
		<img src="<c:url value='/incl/images/example_questions/constant_sum.png'/>"
			alt="<fmt:message key="createQuestion.type.constantSumAlt" />" title="<fmt:message key="createQuestion.type.constantSumAlt" />"
			/>
	</div>
</div>


 
<form action="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>" method='get'>
<div class="buttonBar">
   <input value="<fmt:message key='createQuestion.finishedButton'/>" name="back" type="submit" class="button" /> 
</div>
</form>
