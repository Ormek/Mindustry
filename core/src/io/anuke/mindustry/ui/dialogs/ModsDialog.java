package io.anuke.mindustry.ui.dialogs;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.graphics.*;
import io.anuke.mindustry.mod.Mods.*;
import io.anuke.mindustry.ui.*;

import java.io.*;

import static io.anuke.mindustry.Vars.*;

public class ModsDialog extends FloatingDialog{

    public ModsDialog(){
        super("$mods");
        addCloseButton();
        shown(this::setup);

        hidden(() -> {
            if(mods.requiresReload()){
                ui.loadAnd("$reloading", () -> {
                    mods.reloadContent();
                });
            }
        });

        shown(() -> Core.app.post(() -> {
            Core.settings.getBoolOnce("modsalpha", () -> {
                ui.showText("$mods", "$mods.alphainfo");
            });
        }));
    }

    void setup(){
        cont.clear();
        cont.defaults().width(520f).pad(4);
        cont.add("$mod.reloadrequired").visible(mods::requiresReload).center().get().setAlignment(Align.center);
        cont.row();
        if(!(mods.all().isEmpty() && mods.disabled().isEmpty())){
            cont.pane(table -> {
                table.margin(10f).top();
                Array<LoadedMod> all = Array.withArrays(mods.all(), mods.disabled());

                boolean anyDisabled = false;
                for(LoadedMod mod : all){
                    if(!mod.enabled() && !anyDisabled && mods.all().size > 0){
                        anyDisabled = true;
                        table.row();
                        table.addImage().growX().height(4f).pad(6f).color(Pal.gray);
                    }

                    table.table(Styles.black6, t -> {
                        t.defaults().pad(2).left().top();
                        t.margin(14f).left();
                        t.table(title -> {
                            title.left();
                            title.add("[accent]" + mod.meta.name + "[lightgray] v" + mod.meta.version + (" | " + Core.bundle.get(mod.enabled() ? "mod.enabled" : "mod.disabled")));
                            title.add().growX();

                            title.addImageTextButton(mod.enabled() ? "$mod.disable" : "$mod.enable", mod.enabled() ? Icon.arrowDownSmall : Icon.arrowUpSmall, Styles.cleart, () -> {
                                mods.setEnabled(mod, !mod.enabled());
                                setup();
                            }).height(50f).margin(8f).width(130f);

                            title.addImageButton(Icon.trash16Small, Styles.cleari, () -> ui.showConfirm("$confirm", "$mod.remove.confirm", () -> {
                                mods.removeMod(mod);
                                setup();
                            })).size(50f);
                        }).growX().left().padTop(-14f).padRight(-14f);

                        t.row();
                        if(mod.meta.author != null){
                            t.add(Core.bundle.format("mod.author", mod.meta.author));
                            t.row();
                        }
                        if(mod.meta.description != null){
                            t.labelWrap("[lightgray]" + mod.meta.description).growX();
                            t.row();
                        }

                    }).width(500f);
                    table.row();
                }
            });

        }else{
            cont.table(Styles.black6, t -> t.add("$mods.none")).height(80f);
        }

        cont.row();

        cont.addImageTextButton("$mod.import", Icon.add, () -> {
            platform.showFileChooser(true, "zip", file -> {
                try{
                    mods.importMod(file);
                    setup();
                }catch(IOException e){
                    ui.showException(e);
                    e.printStackTrace();
                }
            });
        }).margin(12f).width(500f);
    }
}