package com.github.olly.workshop.imageorchestrator.config;

import com.github.olly.workshop.imageorchestrator.model.Image;
import com.github.olly.workshop.imageorchestrator.model.Transformation;
import com.github.olly.workshop.imageorchestrator.model.TransformationRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LoggingContextUtil {

    public void mdcPut(Transformation transformation) {
        mdcClear();
        transformation(transformation);
    }

    public void mdcPut(Image image) {
        mdcClear();
        image(image);
    }

    public void mdcPut(TransformationRequest transformationRequest) {
        mdcClear();
        transformationRequest(transformationRequest);
    }

    public void mdcPut(Object... objects) {
        mdcClear();
        for (Object object : objects) {
            if (object instanceof Image) {
                image((Image) object);
            } else if (object instanceof Transformation) {
                transformation((Transformation) object);
            } else if (object instanceof TransformationRequest) {
                transformationRequest((TransformationRequest) object);
            }
        }
    }

    public void mdcClear() {
        MDC.clear();
    }

    private void transformationRequest(TransformationRequest transformationRequest) {
        if (transformationRequest != null) {
            MDC.put("imageId", transformationRequest.getImageId());
            MDC.put("persist", transformationRequest.getPersist().toString());
            MDC.put("transformationType", transformationRequest.getTransformationTypes().toString());
            transformationRequest.getTransformations().forEach(LoggingContextUtil::transformationProperties);
        }
    }

    private void transformation(Transformation transformation) {
        if (transformation != null) {
            MDC.put("transformationType", transformation.getType().name());
            transformationProperties(transformation);
        }
    }

    private static void transformationProperties(Transformation transformation) {
        if (transformation != null && transformation.getProperties() != null) {
            transformation.getProperties().forEach(MDC::put);
        }
    }

    private void image(Image image) {
        if (image != null) {
            MDC.put("mimeType", image.getMimeType());
            MDC.put("imageId", image.getId());
        }
    }
}