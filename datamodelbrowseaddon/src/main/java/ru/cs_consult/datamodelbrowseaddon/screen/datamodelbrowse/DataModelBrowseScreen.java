package ru.cs_consult.datamodelbrowseaddon.screen.datamodelbrowse;

import io.jmix.ui.component.BrowserFrame;
import io.jmix.ui.component.StreamResource;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.cs_consult.datamodelbrowseaddon.service.DataModelService;

import java.io.ByteArrayInputStream;

@UiController("dmb_DataModelBrowseScreen")
@UiDescriptor("datamodelbrowse-screen.xml")
public class DataModelBrowseScreen extends Screen {

    @Autowired
    private BrowserFrame browserFrame;
    @Autowired
    private ApplicationContext applicationContext;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        DataModelService dataModelService = applicationContext.getBean(DataModelService.class);
        browserFrame.setSource(StreamResource.class)
                .setStreamSupplier(() -> new ByteArrayInputStream(dataModelService.getDataModel().getBytes())).setMimeType("text/html");
    }
}