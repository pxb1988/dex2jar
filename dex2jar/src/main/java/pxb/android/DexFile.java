/**
 * 
 */
package pxb.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pxb.android.Anno.Item;
import pxb.util.Hex;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class DexFile {
	private static final Logger log = LoggerFactory.getLogger(DexFile.class);

	public DexFile(byte[] data) {
		this.input = new DataInImpl(data);
		DataIn in = input;
		// 0x 64 65 78
		byte[] magic = in.readBytes(3);
		log.debug("magic:'{}'", new String(magic));
		// 0x 0A ?
		in.readByte();
		// 0x30 33 35
		byte[] version = in.readBytes(3);
		log.debug("version:'{}'", new String(version));
		// 0x 00 ?
		in.readByte();
		// 0x A3 14 18 FB
		int checksum = in.readIntx();
		log.debug("checksum:0x{}", Integer.toHexString(checksum));
		// signiture
		// in.skipBytes(20);
		byte[] signature = in.readBytes(20);
		log.debug("signature:0x{}", Hex.from(signature).encode().toString());
		int fileSize = in.readIntx();
		log.debug("fileSize:{}", fileSize);
		int headSize = in.readIntx();
		log.debug("headSize:{}", headSize);
		int x28h = in.readIntx();
		log.debug("x28h:{} (0x{})", x28h, Integer.toHexString(x28h));
		int link_size = in.readIntx();
		log.debug("link_size:{}", link_size);
		int link_off = in.readIntx();
		log.debug("link_off:{} (0x{})", link_off, Integer.toHexString(link_off));

		int x34h = in.readIntx();
		log.debug("x34h:{}", x34h);

		string_ids_size = in.readIntx();
		log.debug("string_ids_size:{}", string_ids_size);
		string_ids_off = in.readIntx();
		log.debug("string_ids_off:{} (0x{})", string_ids_off, Integer.toHexString(string_ids_off));
		type_ids_size = in.readIntx();
		log.debug("type_ids_size:{} (0x{})", type_ids_size, Integer.toHexString(type_ids_size));
		type_ids_off = in.readIntx();
		log.debug("type_ids_off:{} (0x{})", type_ids_off, Integer.toHexString(type_ids_off));

		proto_ids_size = in.readIntx();
		log.debug("proto_ids_size:{} (0x{})", proto_ids_size, Integer.toHexString(proto_ids_size));
		proto_ids_off = in.readIntx();
		log.debug("proto_ids_off:{} (0x{})", proto_ids_off, Integer.toHexString(proto_ids_off));

		field_ids_size = in.readIntx();
		log.debug("field_ids_size:{} (0x{})", field_ids_size, Integer.toHexString(field_ids_size));
		field_ids_off = in.readIntx();
		log.debug("field_ids_off:{} (0x{})", field_ids_off, Integer.toHexString(field_ids_off));
		method_ids_size = in.readIntx();
		log.debug("method_ids_size:{} (0x{})", method_ids_size, Integer.toHexString(method_ids_size));
		method_ids_off = in.readIntx();
		log.debug("method_ids_off:{} (0x{})", method_ids_off, Integer.toHexString(method_ids_off));

		class_defs_size = in.readIntx();
		log.debug("class_defs_size:{} (0x{})", class_defs_size, Integer.toHexString(class_defs_size));

		class_defs_off = in.readIntx();
		log.debug("class_defs_off:{} (0x{})", class_defs_off, Integer.toHexString(class_defs_off));

		data_size = in.readIntx();
		log.debug("data_size:{} (0x{})", data_size, Integer.toHexString(data_size));

		data_off = in.readIntx();
		log.debug("data_off:{} (0x{})", data_off, Integer.toHexString(data_off));
		log.debug("=======End Of Head========");
	}

	// public DexFile(InputStream is) {
	//
	// }
	DataIn input;
	int string_ids_size;
	int string_ids_off;
	int type_ids_size;
	int type_ids_off;
	private int proto_ids_size;
	private int proto_ids_off;

	int field_ids_size;
	int field_ids_off;
	int method_ids_size;
	int method_ids_off;
	int class_defs_size;
	int class_defs_off;
	int data_size;
	int data_off;

	public static class Member {
		String owner;
		String name;
		String type;
	}

	/**
	 * 一个String id为4字节
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public String getStringItem(int id) {
		if (id >= this.string_ids_size)
			throw new RuntimeException("Id out of bound");
		DataIn in = input;
		int idxOffset = this.string_ids_off + id * 4;
		// log.debug("string_idx_offset:0x{}", Integer.toHexString(idxOffset));
		in.pushMove(idxOffset);
		int offset = in.readIntx();
		in.pushMove(offset);
		int length = in.readUnsignedByte();
		String string = new String(in.readBytes(length));
		// log.debug("{}({},{},{})='{}'", new Object[] { id,
		// Integer.toHexString(idxOffset), Integer.toHexString(offset), length,
		// string });
		in.pop();
		in.pop();
		return string;
	}

	public Proto getProto(int id) {
		if (id >= this.proto_ids_size)
			throw new RuntimeException("Id out of bound");
		DataIn in = input;
		int idxOffset = this.proto_ids_off + id * 12;
		// log.debug("proto_idx_offset:0x{}", Integer.toHexString(idxOffset));
		in.pushMove(idxOffset);
		Proto proto = new Proto(this, in);
		in.pop();
		return proto;
	}

	public String getTypeItem(int id) {
		if (id >= this.type_ids_size)
			throw new RuntimeException("Id out of bound");
		DataIn in = input;
		int idxOffset = this.type_ids_off + id * 4;
		// log.debug("type_idx_offset:0x{}", Integer.toHexString(idxOffset));
		in.pushMove(idxOffset);
		int offset = in.readIntx();
		String desc = this.getStringItem(offset);
		in.pop();
		return desc;
	}

	public Method getMethod(int id) {
		if (id >= this.method_ids_size)
			throw new RuntimeException("Id out of bound");
		DataIn in = input;
		int idxOffset = this.method_ids_off + id * 8;
		// log.debug("method_idx_offset:0x{}", Integer.toHexString(idxOffset));
		in.pushMove(idxOffset);
		Method m = new Method(this, in);
		in.pop();
		return m;
	}

	public Field getField(int id) {
		if (id >= this.field_ids_size)
			throw new RuntimeException("Id out of bound");
		DataIn in = input;
		int idxOffset = this.field_ids_off + id * 8;
		// log.debug("field_idx_offset:0x{}", Integer.toHexString(idxOffset));
		in.pushMove(idxOffset);
		Field m = new Field(this, in);
		in.pop();
		return m;
	}

	private void accept(DataIn in, ClassVisitor cv) {
		int class_idx = in.readIntx();
		cv = new ClassNameAdapter(cv);
		String className = this.getTypeItem(class_idx);
		log.debug("class_idx:{} '{}'", class_idx, className);

		int access_flags = in.readIntx();
		log.debug("access_flags:{} (0x{})", access_flags, Integer.toHexString(access_flags));

		int superclass_idx = in.readIntx();
		String superClassName = superclass_idx == -1 ? null : this.getTypeItem(superclass_idx);
		log.debug("superclass_idx:{} '{}'", superclass_idx, superClassName);

		// 获取接口
		List<String> interfaceTypeList = new ArrayList<String>();
		{
			int interfaces_off = in.readIntx();
			log.debug("interfaces_off:{} (0x{})", interfaces_off, Integer.toHexString(interfaces_off));
			if (interfaces_off != 0) {
				in.pushMove(interfaces_off);
				int size = in.readIntx();
				for (int i = 0; i < size; i++) {
					String p = getTypeItem(in.readShortx());
					interfaceTypeList.add(p);
				}
				in.pop();
			}
		}
		// 获取源文件
		String sourceFile = "<none>";
		{
			int source_file_idx = in.readIntx();
			if (source_file_idx != -1)
				sourceFile = this.getStringItem(source_file_idx);
			log.debug("source_file_idx:{} '{}'", source_file_idx, sourceFile);
		}
		// 获取注解
		Anno[] classAnnos = null;
		Map<Integer, Anno[]> fieldAnnos = new HashMap<Integer, Anno[]>();
		Map<Integer, Anno[]> methodAnnos = new HashMap<Integer, Anno[]>();
		Map<Integer, Anno[][]> parameterAnnos = new HashMap<Integer, Anno[][]>();
		{
			int annotations_off = in.readIntx();
			if (annotations_off != 0) {
				log.debug("annotations_off:{} (0x{})", annotations_off, Integer.toHexString(annotations_off));
				in.pushMove(annotations_off);
				int class_annotations_off = in.readIntx();
				int field_annotation_size = in.readIntx();
				int method_annotation_size = in.readIntx();
				int parameter_annotation_size = in.readIntx();
				if (class_annotations_off != 0) {
					in.pushMove(class_annotations_off);
					int size = in.readIntx();
					classAnnos = new Anno[size];
					for (int j = 0; j < size; j++) {
						int field_annotation_offset = in.readIntx();
						in.pushMove(field_annotation_offset);
						classAnnos[j] = new Anno(this, in);
						log.debug(classAnnos[j].toString());
						in.pop();
					}
					in.pop();
				}
				{
					for (int i = 0; i < field_annotation_size; i++) {
						int field_idx = in.readIntx();
						int field_annotations_offset = in.readIntx();
						in.pushMove(field_annotations_offset);
						int size = in.readIntx();
						Anno[] annos = new Anno[size];
						for (int j = 0; j < size; j++) {
							int field_annotation_offset = in.readIntx();
							in.pushMove(field_annotation_offset);
							annos[j] = new Anno(this, in);
							log.debug(annos[j].toString());
							in.pop();
						}
						fieldAnnos.put(field_idx, annos);
						in.pop();
					}
				}

				{

					for (int i = 0; i < method_annotation_size; i++) {
						int method_idx = in.readIntx();
						int method_annotation_offset = in.readIntx();
						in.pushMove(method_annotation_offset);
						int size = in.readIntx();
						Anno[] annos = new Anno[size];
						for (int j = 0; j < size; j++) {
							int field_annotation_offset = in.readIntx();
							in.pushMove(field_annotation_offset);
							annos[j] = new Anno(this, in);
							log.debug(annos[j].toString());
							in.pop();
						}
						methodAnnos.put(method_idx, annos);
						in.pop();
					}
				}
				{
					for (int i = 0; i < parameter_annotation_size; i++) {
						int method_idx = in.readIntx();
						int parameter_annotation_offset = in.readIntx();
						in.pushMove(parameter_annotation_offset);
						int sizeJ = in.readIntx();
						Anno[][] annoss = new Anno[sizeJ][];
						for (int j = 0; j < sizeJ; j++) {
							int field_annotation_offset = in.readIntx();
							in.pushMove(field_annotation_offset);
							int sizeK = in.readIntx();
							Anno[] annos = new Anno[sizeK];
							for (int k = 0; k < sizeK; k++) {
								int offsetK = in.readIntx();
								in.pushMove(offsetK);
								Anno anno = new Anno(this, in);
								log.debug(anno.toString());
								annos[k] = anno;
								in.pop();
							}
							annoss[j] = annos;
							in.pop();
						}
						parameterAnnos.put(method_idx, annoss);
						in.pop();
					}
				}
				in.pop();
			}
		}

		int class_data_off = in.readIntx();
		log.debug("class_data_off:{} (0x{})", class_data_off, Integer.toHexString(class_data_off));
		Object[] constant = null;
		{
			int static_values_off = in.readIntx();
			log.debug("static_values_off:{} (0x{})", static_values_off, Integer.toHexString(static_values_off));
			if (static_values_off != 0) {
				in.pushMove(static_values_off);
				int size = in.readUnsignedLeb128();
				constant = new Object[size];
				for (int i = 0; i < size; i++) {
					constant[i] = Constant.ReadConstant(this, in);
				}
				in.pop();
			}
		}

		// //////////////////////////////////////////////////////////////
		// TODO signature
		cv.visit(Opcodes.V1_5, access_flags, className, null, superClassName, interfaceTypeList.toArray(new String[interfaceTypeList.size()]));
		cv.visitSource(sourceFile, null);
		// visit class annotation
		if (classAnnos != null) {
			for (Anno anno : classAnnos) {
				anno.accept(cv);
			}
			classAnnos = null;
		}
		// //////////////////////////////////////////////////////////////

		in.pushMove(class_data_off);
		int static_fields = in.readByte();
		log.debug("static_fields:{} (0x{})", static_fields, Integer.toHexString(static_fields));
		int instance_fields = in.readByte();
		log.debug("instance_fields:{} (0x{})", instance_fields, Integer.toHexString(instance_fields));
		int direct_methods = in.readByte();
		log.debug("direct_methods:{} (0x{})", direct_methods, Integer.toHexString(direct_methods));
		int virtual_methods = in.readByte();
		log.debug("virtual_methods:{} (0x{})", virtual_methods, Integer.toHexString(virtual_methods));
		{
			int lastIndex = 0;
			for (int i = 0; i < static_fields; i++) {
				int diff = in.readUnsignedLeb128();
				int field_id = lastIndex + diff;
				lastIndex = field_id;
				Field field = getField(field_id);
				log.debug("field:[{}] {}", new Object[] { field_id, field });

				int field_access_flags = in.readUnsignedLeb128();

				// //////////////////////////////////////////////////////////////
				// TODO signature, value
				Object fvalue = null;
				if (constant != null && i < constant.length) {
					fvalue = constant[i];
				}
				FieldVisitor fv = cv.visitField(field_access_flags, field.getName(), field.getType(), null, fvalue);
				if (fv != null) {
					Anno[] fannos = fieldAnnos.get(field_id);
					if (fannos != null) {
						for (Anno anno : fannos) {
							anno.accept(fv);
						}
					}
					fv.visitEnd();
				}
				// //////////////////////////////////////////////////////////////
			}
		}
		{
			int lastIndex = 0;
			for (int i = 0; i < instance_fields; i++) {
				int diff = in.readUnsignedLeb128();
				int field_id = lastIndex + diff;
				lastIndex = field_id;
				Field field = getField(field_id);
				log.debug("field:[{}] {}", new Object[] { field_id, field });
				int field_access_flags = in.readUnsignedLeb128();
				// //////////////////////////////////////////////////////////////
				// TODO signature, value
				FieldVisitor fv = cv.visitField(field_access_flags, field.getName(), field.getType(), null, null);
				if (fv != null) {
					Anno[] fannos = fieldAnnos.get(field_id);
					if (fannos != null) {
						for (Anno anno : fannos) {
							anno.accept(fv);
						}
					}
					fv.visitEnd();
				}
				// //////////////////////////////////////////////////////////////
			}
		}
		{
			int lastIndex = 0;
			for (int i = 0; i < direct_methods; i++) {
				int diff = in.readUnsignedLeb128();
				int method_id = lastIndex + diff;
				lastIndex = method_id;
				Method method = getMethod(method_id);
				log.debug("method:[{}] {}", new Object[] { method_id, method });

				int method_access_flags = in.readUnsignedLeb128();
				log.debug("method_access_flags:{} (0x{})", method_access_flags, Integer.toHexString(method_access_flags));

				int code_off = in.readUnsignedLeb128();
				log.debug("code_off:{} (0x{})", code_off, Integer.toHexString(code_off));

				// //////////////////////////////////////////////////////////////
				// TODO signature,exception
				String[] exceptions = null;
				{// Find exceptions
					Anno[] fannos = methodAnnos.get(method_id);
					if (fannos != null) {
						for (Anno anno : fannos) {
							if (anno.getType().equals("Ldalvik/annotation/Throws;")) {
								Item[] items = anno.getItems();
								for (Item it : items) {
									if (it.getName().equals("value")) {
										Object[] value = (Object[]) it.getValue();
										exceptions = new String[value.length];
										for (int j = 0; j < value.length; j++) {
											exceptions[j] = value[j].toString();
										}
									}
								}
							}
						}
					}
				}
				MethodVisitor mv = cv.visitMethod(method_access_flags, method.getName(), method.getType().getDesc(), null, exceptions);
				if (mv != null) {
					mv = new FilterAnnotationAdapter(mv);
					{
						Anno[] fannos = methodAnnos.get(method_id);
						if (fannos != null) {
							for (Anno anno : fannos) {
								anno.accept(mv);
							}
						}
					}
					{
						Anno[][] fannoss = parameterAnnos.get(method_id);
						if (fannoss != null) {
							for (int j = 0; j < fannoss.length; j++) {
								Anno[] fannos = fannoss[j];
								for (Anno anno : fannos) {
									anno.accept(j, mv);
								}
							}
						}
					}
					if (code_off != 0) {
						in.pushMove(code_off);
						mv.visitCode();
						new Code(this, in, method, true).accept(new DexMethodVisitor(mv));
						in.pop();
					}
					mv.visitEnd();
				}
				// //////////////////////////////////////////////////////////////
				log.debug("=====");
			}
		}
		{
			int lastIndex = 0;
			for (int i = 0; i < virtual_methods; i++) {
				int diff = in.readUnsignedLeb128();
				int method_id = lastIndex + diff;
				lastIndex = method_id;
				Method method = getMethod(method_id);
				log.debug("method:[{}] {}", new Object[] { method_id, method });

				int method_access_flags = in.readUnsignedLeb128();
				log.debug("method_access_flags:{} (0x{})", method_access_flags, Integer.toHexString(method_access_flags));

				int code_off = in.readUnsignedLeb128();
				log.debug("code_off:{} (0x{})", code_off, Integer.toHexString(code_off));

				// //////////////////////////////////////////////////////////////
				// TODO signature,exception

				String[] exceptions = null;
				{// Find exceptions
					Anno[] fannos = methodAnnos.get(method_id);
					if (fannos != null) {
						for (Anno anno : fannos) {
							if (anno.getType().equals("Ldalvik/annotation/Throws;")) {
								Item[] items = anno.getItems();
								for (Item it : items) {
									if (it.getName().equals("value")) {
										Object[] value = (Object[]) it.getValue();
										exceptions = new String[value.length];
										for (int j = 0; j < value.length; j++) {
											exceptions[j] = value[j].toString();
										}
									}
								}
							}
						}
					}
				}
				MethodVisitor mv = cv.visitMethod(method_access_flags, method.getName(), method.getType().getDesc(), null, exceptions);
				if (mv != null) {
					mv = new FilterAnnotationAdapter(mv);
					{
						Anno[] fannos = methodAnnos.get(method_id);
						if (fannos != null) {
							for (Anno anno : fannos) {
								anno.accept(mv);
							}
						}
					}
					{
						Anno[][] fannoss = parameterAnnos.get(method_id);
						if (fannoss != null) {
							for (int j = 0; j < fannoss.length; j++) {
								Anno[] fannos = fannoss[j];
								for (Anno anno : fannos) {
									anno.accept(j, mv);
								}
							}
						}
					}
					if (code_off != 0) {
						in.pushMove(code_off);
						mv.visitCode();
						new Code(this, in, method, false).accept(new DexMethodVisitor(mv));
						in.pop();
					}
					mv.visitEnd();
				}
				// //////////////////////////////////////////////////////////////

				log.debug("=====");
			}
		}
		in.pop();
	}

	public void accept(ClassVisitorFactory factory) {
		DataIn in = input;
		for (int cid = 0; cid < class_defs_size; cid++) {

			log.debug("=======Start Of Class [{}]========", cid);
			int idxOffset = this.class_defs_off + cid * 32;
			// log.debug("class_idx_offset:0x{}",
			// Integer.toHexString(idxOffset));

			in.pushMove(idxOffset);
			try {
				ClassVisitor cv = factory.create();
				this.accept(in, cv);
				cv.visitEnd();
			} catch (Exception e) {
				log.error("Fail on class {} cause: [{}]", cid, e);
			}
			in.pop();
			log.debug("=======End Of Class [{}]========", cid);

		}
		log.debug("Finish.");
	}
}
