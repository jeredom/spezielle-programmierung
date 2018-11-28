package reissverschlussverfahren.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import org.piccolo2d.nodes.PText;

import repast.simphony.visualization.editedStyle.EditedStyle2D;
import repast.simphony.visualization.editedStyle.EditedStyleData;
import repast.simphony.visualization.editedStyle.EditedStyleUtils;
import repast.simphony.visualization.visualization2D.style.Style2D;

//public class CarStyle extends EditedStyle2D implements Style2D<Object> {
public class CarStyle extends EditedStyle2D {
//	EditedStyleData<Object> innerStyle;
//	private BufferedImage image;
//	private Image image;
//	String iconFile = null; 

	public CarStyle(String userStyleFile) {

		super(userStyleFile);
	}

//	@Override
//	public PText getLabel(Object object) {
//		

//		//PText newLabel = super.getLabel(object);

//		PText newLabel = new PText();
//
//		PText label = null;
//
//		label = new PText(EditedStyleUtils.getLabel(innerStyle, object));
//
//		Font font = new Font(innerStyle.getLabelFontFamily(), 
//				innerStyle.getLabelFontType(),
//				innerStyle.getLabelFontSize());
//
//		if ("".equals(label.getText()))
//			return null;
//
//		label.setFont(font);
//
//		double offSet = innerStyle.getLabelOffset();
//
//		double xOffSet = 0;
//		double yOffSet = 0;
//
//		double width = getBounds(object).getWidth();
//		double height = getBounds(object).getHeight();
//
//		// right
//		if ("right".equals(innerStyle.getLabelPosition())){
//			xOffSet = width + offSet;
//			yOffSet = height/2 + label.getBounds().height/2;		}
//		// left
//		else if ("left".equals(innerStyle.getLabelPosition())){
//			xOffSet = -label.getBounds().width - offSet;
//			yOffSet = height/2 + label.getBounds().height/2;
//		}
//		// top
//		else if ("top".equals(innerStyle.getLabelPosition())){
//			xOffSet = -(label.getWidth()/2 - width/2);
//			yOffSet = height + label.getBounds().height + offSet;
//		}
//		else{
//			xOffSet = -(label.getWidth()/2 - width/2);
//			yOffSet = - offSet;
//		}
//		label.setOffset(xOffSet,yOffSet);
//
//		float colorRGB[] = innerStyle.getLabelColor();
//
//		label.setTextPaint(new Color(colorRGB[0],colorRGB[1],colorRGB[2]));
//		PText newLabel = label;
//		
//		//newLabel.setOffset(newLabel.getWidth(), newLabel.getHeight());
//		newLabel.setOffset(10, 10);
//		
//		//newLabel.setTextPaint(new Color(255,165,0));
//		return newLabel;
//	}

}
