package com.spid.batch.jobs.utils;

import com.oxit.spid.core.bo.spid.Functionality;
import com.oxit.spid.core.bo.spid.Juridical;
import com.oxit.spid.core.bo.spid.Model;
import com.oxit.spid.core.bo.spid.Product;
import com.oxit.spid.core.bo.spid.ProductMarketing;
import com.oxit.spid.core.bo.spid.ProductValidationInfos;
import com.oxit.spid.core.bo.spid.ProductWarrantly;
import com.oxit.spid.core.bo.spid.UserBenefit;
import com.oxit.spid.transline.xmlsp.BENEFSUTILISATEUR;
import com.oxit.spid.transline.xmlsp.FUNCTIONNALITY;
import com.oxit.spid.transline.xmlsp.TEXTPRODUCT;
import com.oxit.spid.transline.xmlsp.TRANSLATE;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class XmlToBusinessObjectTranslineHelper {

	public static Model convert(TRANSLATE document) {
		Model model = new Model();

		TEXTPRODUCT textProductDocument = document.getTEXTPRODUCT().get(0);

		model.setModelName(textProductDocument.getLIBELLE().getValue());
		model.setWebLabel(safeGet(textProductDocument.getLIBELLEWEB(), () -> textProductDocument.getLIBELLEWEB().getValue()));
		model.setNutritionTable(safeGet(textProductDocument.getNUTRITIONTABLE(), () -> textProductDocument.getNUTRITIONTABLE().getValue()));
		model.setWeightContext(safeGet(textProductDocument.getCONTEXTEPOIDS(), () -> textProductDocument.getCONTEXTEPOIDS().getValue()));
		model.setProduct(buildProduct(textProductDocument));
		return model;
	}

	private static Product buildProduct(TEXTPRODUCT textProductDocument) {
		Product product = new Product();
		product.setProductNature(safeGet(textProductDocument.getNATUREPRODUIT(), () -> textProductDocument.getNATUREPRODUIT().getValue()));
		product.setProductMarketing(buildProductMarketing(textProductDocument));
		product.setUserBenefits(buildUserBenefits(textProductDocument.getBENEFSUTILISATEURS().getBENEFSUTILISATEUR()));
		product.setFunctionalities(buildFunctionalities(textProductDocument.getFUNCTIONNALITIES().getFUNCTIONNALITY()));
		ProductValidationInfos validationInfos = new ProductValidationInfos();
		validationInfos.setTestText(safeGet(textProductDocument.getTESTLABTERRAIN(), () -> textProductDocument.getTESTLABTERRAIN().getValue()));
		validationInfos.setAgreeBy(safeGet(textProductDocument.getAPPROUVEPAR(), () -> textProductDocument.getAPPROUVEPAR().getValue()));
		product.setValidationInfos(validationInfos);
		product.setJuridical(new Juridical());
		product.getJuridical().setLegalNotice(safeGet(textProductDocument.getLEGALNOTICE(), () -> textProductDocument.getLEGALNOTICE().getValue()));
		return product;
	}

	private static ProductMarketing buildProductMarketing(TEXTPRODUCT textProductDocument) {
		ProductMarketing productMarketing = new ProductMarketing();
		productMarketing.setMadeForExtended(textProductDocument.getNOUVEAUCONCUPOUR() != null ? RichTextUtils.extractText(textProductDocument.getNOUVEAUCONCUPOUR()) : StringUtils.EMPTY);
		productMarketing.setComposition(safeGet(textProductDocument.getCOMPOSITION(), () -> textProductDocument.getCOMPOSITION().getValue()));
		productMarketing.setMaintenanceAdvice(safeGet(textProductDocument.getCONSEILENTRETIEN(), () -> textProductDocument.getCONSEILENTRETIEN().getValue()));
		productMarketing.setStorageAdvice(safeGet(textProductDocument.getCONSEILSTOCKAGE(), () -> textProductDocument.getCONSEILSTOCKAGE().getValue()));
		productMarketing.setTeaser(safeGet(textProductDocument.getPRODUCTCATCHLINE(), () -> textProductDocument.getPRODUCTCATCHLINE().getValue()));
		productMarketing.setInternetTeaser(safeGet(textProductDocument.getPRODUCTCATCHLINEINTERNET(), () -> textProductDocument.getPRODUCTCATCHLINEINTERNET().getValue()));
		productMarketing.setUserInstruction(safeGet(textProductDocument.getRESTRICTIONUSAGE(), () -> textProductDocument.getRESTRICTIONUSAGE().getValue()));
		productMarketing.setTechnicalComposant(safeGet(textProductDocument.getCOMPOSANTSTECHNIQUES(), () -> textProductDocument.getCOMPOSANTSTECHNIQUES().getValue()));
		ProductWarrantly warranty = new ProductWarrantly();
		warranty.setWarrantlyText(safeGet(textProductDocument.getGARANTIE(), () -> textProductDocument.getGARANTIE().getValue()));
		productMarketing.setProductWarrantly(warranty);
		return productMarketing;
	}

	private static List<Functionality> buildFunctionalities(List<FUNCTIONNALITY> functionalitiesDocument) {
		return functionalitiesDocument.stream().map(functionalityDocument -> {
			Functionality functionality = new Functionality();
			functionality.setFuncType(functionalityDocument.getTYPE().getValue());
			functionality.setFuncValue(functionalityDocument.getVALEUR().getValue());
			return functionality;
		}).collect(Collectors.toList());
	}

	private static List<UserBenefit> buildUserBenefits(List<BENEFSUTILISATEUR> benefitsUtilisateur) {
		return benefitsUtilisateur.stream().map(XmlToBusinessObjectTranslineHelper::toUserBenefit).toList();
	}

	private static UserBenefit toUserBenefit(BENEFSUTILISATEUR document) {
		UserBenefit userBenefit = new UserBenefit();
		boolean isGooddaUserBenefit = !StringUtils.isEmpty(document.getIdMD());
		userBenefit.setGooddaUserBenefit(isGooddaUserBenefit);
		userBenefit.setId(isGooddaUserBenefit ? document.getIdMD() : document.getId());
		userBenefit.setText(document.getBUVALEUR().getValue());
		return userBenefit;
	}

	private static String safeGet(Object obj, Supplier<String> value) {
		return Optional.ofNullable(obj).map(notNull -> value.get()).orElse(StringUtils.EMPTY);
	}

}
