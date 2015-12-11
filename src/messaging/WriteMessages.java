package messaging;

//Include the following imports to use service bus APIs
import java.util.List;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.*;
import com.microsoft.windowsazure.services.servicebus.models.*;
import com.microsoft.windowsazure.core.*;
import com.microsoft.windowsazure.exception.ServiceException;

import customservicebusexceptions.SubscriptionExistsException;
import customservicebusexceptions.TopicExistsException;

import javax.xml.datatype.*;


/**
 * Static methods that help communication with Azure's Service Bus Contract.
 * @author Gregory Mususa (081587717)
 *
 */
public class WriteMessages {
		
	/**
	 * Checks if a rule exists, for a given Service Bus Contract and Topic
	 * @param ruleInfoName is the rule that will be checked, for existence
	 * @param subInfo is the Subscription this will check in
	 * @param topicInfo is the Topic this will will check in
	 * @param service is the service this will check in
	 * @return <code>true</code> if found, <code>false</code> otherwise
	 * @throws ServiceException
	 */
	public static final boolean ruleExists(String ruleInfoName,SubscriptionInfo subInfo, TopicInfo topicInfo, ServiceBusContract service) throws ServiceException {
		boolean ruleExists = false;
		for(RuleInfo ri : service.listRules(topicInfo.getPath(), subInfo.getName()).getItems()) {
			if(ri.getName().equalsIgnoreCase(ruleInfoName)) {
				ruleExists = true;
			}
		}
		return ruleExists;
	}
	
	/**
	 * Checks if a subscription exists, within the specified Service Bus Contract and Topic
	 * @param subInfo is the subscription that will be checked, for existence
	 * @param topicInfo
	 * @param service
	 * @return
	 * @throws ServiceException
	 */
	public static final boolean subscriptionExists(String subName, TopicInfo topicInfo, ServiceBusContract service) throws ServiceException {
		boolean subscriptionExists = false;
		for(SubscriptionInfo si : service.listSubscriptions(topicInfo.getPath()).getItems()) {
			if(si.getName().equalsIgnoreCase(subName)) {
				subscriptionExists = true;
			}
		}
		return subscriptionExists;
	}
	
	/**
	 * Create a new subscription under a the given Topic/Service, if it does not exist / Else get subscription
	 * @param subName is the name of the new Subscription
	 * @param topicInfo is the Topic where the Subscription will be created
	 * @param service is the Service Bus Contract where the Subscription will be created
	 * @return the created/retrieved subscription, as <code>SubscriptionInfo</code> (if successful) / returns null otherwise
	 * @throws ServiceException
	 * @throws SubscriptionExistsException
	 */
	public static final SubscriptionInfo initializeSubscription(String subName, TopicInfo topicInfo, ServiceBusContract service) throws ServiceException, SubscriptionExistsException {
		SubscriptionInfo subInfo = null;
		
		if(WriteMessages.subscriptionExists(subName, topicInfo, service)) {
			subInfo = service.getSubscription(topicInfo.getPath(), subName).getValue();
		}
		else {
			subInfo = new SubscriptionInfo(subName);
			
			if(!(WriteMessages.subscriptionExists(subInfo.getName(), topicInfo, service))) {
				CreateSubscriptionResult result = service.createSubscription(topicInfo.getPath(), subInfo);
			}
			else {
				throw new SubscriptionExistsException();
			}
		}
		return subInfo;
	}
	
	/**
	 * Checks if a topic name exists, within the specified Service Bus Contract
	 * @param topicInfo is the topic that will be checked, for existence
	 * @param service is the Service Bus Contract we will check in
	 * @return <code>true</code> if topic exists, <code>false</code> if topic doesn't exist
	 * @throws ServiceException
	 */
	public static final boolean topicExists(String topicInfoName, ServiceBusContract service) throws ServiceException {
		boolean topicExists = false;			
		for(TopicInfo t : service.listTopics().getItems()) {
			if(topicInfoName.equalsIgnoreCase(t.getPath())) {
				topicExists = true;
			}
		}
		return topicExists;
	}
	
	/**
	 * Create a new topic within the specified Service Bus Contract, if it does not exist / Else get topic 
	 * @param topicName is the name of the new topic
	 * @param service is the Service Bus Contract where the Topic will be created
	 * @return the created/retrieved topic, as <code>TopicInfo</code> (if successful) / returns null otherwise
	 * @throws ServiceException
	 * @throws TopicExistsException
	 */
	public static final TopicInfo initializeTopic(String topicName, ServiceBusContract service) throws ServiceException, TopicExistsException {
		TopicInfo topicInfo = null;
		
		if(WriteMessages.topicExists(topicName, service)) {
			topicInfo = service.getTopic(topicName).getValue();
		}
		else {
			topicInfo = new TopicInfo(topicName);
			
			if(!(WriteMessages.topicExists(topicInfo.getPath(), service))) {
				CreateTopicResult result = service.createTopic(topicInfo);
			}
			else {
				throw new TopicExistsException();
			}
		}
		return topicInfo;
	}
}
